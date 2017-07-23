package com.radix.cmake.xdebug

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.execution.process.ProcessInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.attach.XLocalAttachDebugger
import com.intellij.xdebugger.attach.XLocalAttachDebuggerProvider
import com.intellij.xdebugger.attach.XLocalAttachGroup
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import org.apache.http.entity.mime.MIME.UTF8_CHARSET
import org.jetbrains.annotations.NotNull
import java.awt.Image.SCALE_SMOOTH
import java.util.concurrent.Callable
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.Future
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.ArrayList
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon


class LocalAttachDebugger : XLocalAttachDebuggerProvider {
    var attachGroup = CMakeLocalAttachGroup()
    override fun getAttachGroup(): XLocalAttachGroup {
        return attachGroup
    }

    override fun getAvailableDebuggers(project: Project, processInfo: ProcessInfo, contextHolder: UserDataHolder): MutableList<XLocalAttachDebugger> {
        if(processInfo.executableName.startsWith("cmake")){
            return mutableListOf( CMakeLocalAttachDebugger(processInfo))
        }
        return mutableListOf()
    }

}

class CMakeLocalAttachGroup : XLocalAttachGroup{
    override fun getProcessDisplayText(project: Project, info: ProcessInfo, dataHolder: UserDataHolder): String {
        return info.executableName + " " + info.args
    }

    override fun getProcessIcon(project: Project, info: ProcessInfo, dataHolder: UserDataHolder): Icon {
        return ImageIcon(ImageIO.read( this.javaClass.classLoader.getResource( "cmake-icon.png" ) ).getScaledInstance(16, 16, SCALE_SMOOTH))
    }

    override fun getGroupName(): String {
        return "CMake Processes"
    }

    override fun compare(project: Project, a: ProcessInfo, b: ProcessInfo, dataHolder: UserDataHolder): Int {
        return a.pid - b.pid
    }

    override fun getOrder(): Int {
        return 0
    }
}

class CMakeLocalAttachDebugger(processInfo: ProcessInfo) : XLocalAttachDebugger {
    override fun attachDebugSession(project: Project, processInfo: ProcessInfo) {
        FindPortByPID(project, processInfo)
    }

    override fun getDebuggerDisplayName(): String {
        return "CMake Debugger"
    }

}

abstract class JsonServer {
    private val selector = Selector.open()
    private var _isRunning = false
    var isRunning: Boolean
        get() = _isRunning
        set(value) {
            _isRunning = value
        }
    var timeout = 0

    fun addSocket(socket : SocketChannel) {
        socket.configureBlocking(false)
        socket.register(this.selector, SelectionKey.OP_READ)
    }

    fun startThread() : Boolean {
        var server = this
        val t = object : Thread() {
            override fun run() {
                server.start()
            }
        }
        t.start()
        return true
    }

    private fun read(key: SelectionKey) {
        val channel = key.channel() as SocketChannel
        val buffer = ByteBuffer.allocate(1024)
        var numRead = -1
        numRead = channel.read(buffer)

        if (numRead == -1) {
            val socket = channel.socket()
            channel.close()
            key.cancel()
            return
        }

        val data = ByteArray(numRead)
        System.arraycopy(buffer.array(), 0, data, 0, numRead)
        OnRead(key, String(data, UTF8_CHARSET))
    }

    var bracesDepth = 0
    var readBuffer = ""
    private fun OnRead(key: SelectionKey, data : String) {
        for( c : Char in data) {
            if(c == '{') bracesDepth++
            if(c == '}') bracesDepth--
            readBuffer += c
            if(bracesDepth == 0) {
                if(readBuffer.startsWith("[== \"CMake Server\" ==[\n")) {
                    readBuffer = readBuffer.substring("[== \"CMake Server\" ==[\n".length)
                }
                if(readBuffer.endsWith("]== \"CMake Server\" ==]")) {
                    readBuffer = readBuffer.substring(0, readBuffer.length - "]== \"CMake Server\" ==]".length).toString()
                }
                readBuffer = readBuffer.trim()

                try {
                    if(readBuffer.isNotEmpty() && readBuffer[0] == '{') {
                        val parser = JsonParser()
                            val json = parser.parse(readBuffer)
                            if (json != null)
                                processMessage(key, json!!)
                    }
                } catch (e: Exception) {
                    print(e)
                    readBuffer = ""
                }
                readBuffer = ""
            }
        }
    }

    abstract fun  processMessage(key: SelectionKey, json: JsonElement)

    fun start() : Boolean {
        if(isRunning)
            return false
        isRunning = true
        while (isRunning) {
            try {
                // wait for events
                var readyCount = this.selector.select(timeout.toLong())
                if(readyCount == 0 && timeout != 0)
                    isRunning = false
                //work on selected keys
                val keys = this.selector.selectedKeys().iterator()
                while (keys.hasNext()) {
                    val key = keys.next()

                    // this is necessary to prevent the same key from coming up
                    // again the next time around.
                    // keys.remove()

                    if (!key.isValid) {
                        continue
                    }

                    if (key.isReadable) {
                        this.read(key)
                    }

                }
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

}

class FindPortByPID(var project: Project, processInfo: ProcessInfo) : JsonServer() {
    var targetPID = processInfo.pid

    fun portIsOpen(es: ExecutorService, port: Int, timeout: Int): Future<SocketChannel?> {
        return es.submit(Callable {
            try {
                var rtn = SocketChannel.open(InetSocketAddress("localhost", port))
                println("port open:" + port)
                return@Callable rtn
            } catch (ex: Exception) {
                return@Callable null
            }
        })
    }

    fun findSockets(): ArrayList<SocketChannel> {
        val es = Executors.newFixedThreadPool(20)
        val timeout = 200
        val futures = (4329..4379).mapTo(ArrayList<Future<SocketChannel?>>()) { portIsOpen(es, it, timeout) }
        es.shutdown()
        var openPorts = 0

        var sockets = ArrayList<SocketChannel>()
        for (f in futures) {
            val socket = f.get()
            if(socket != null)
                sockets.add(socket)
        }
        return sockets
    }

    override fun processMessage(key: SelectionKey, json: JsonElement) {
        if(json.isJsonObject) {
            var obj = json.asJsonObject!!
            if (obj["State"] != null && obj["State"].isJsonPrimitive()) {

                if (obj["PID"] != null && obj["PID"].isJsonPrimitive) {
                    var clientPID = obj["PID"].asInt
                    key.cancel()
                    if(targetPID == clientPID) {
                        startDebugger(key)
                        isRunning = false
                    } else {
                        key.channel().close()
                    }
                }
            }
        }
    }

    private fun  startDebugger(key: SelectionKey) {

        val debuggerProxy = CMakeDebuggerProxy(key.channel() as SocketChannel)

        ApplicationManager.getApplication().invokeLater {
            val session = XDebuggerManager.getInstance(project).startSessionAndShowTab("CMake debug", null,
                    object : XDebugProcessStarter() {
                        @NotNull
                        override fun start(@NotNull session: XDebugSession): XDebugProcess {
                            return CMakeAttachProcess(session, debuggerProxy)
                        }
                    })
        }
    }

    init {
        timeout = 1000
        var sockets = findSockets()
        for(socket in sockets)
            addSocket(socket)
        startThread()
    }
}

interface JsonServerListener {
    fun processMessage(key: SelectionKey, json: JsonElement);
}

class JsonServerWEvents(var listener : JsonServerListener) : JsonServer() {
    override fun processMessage(key: SelectionKey, json: JsonElement) {
        listener.processMessage(key, json)
    }

}


class CMakeAttachProcess(session: XDebugSession, debuggerProxy: CMakeDebuggerProxy) : CMakeDebugProcess(session, debuggerProxy) {

}
