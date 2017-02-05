package com.radix.cmake.xdebug

import com.fasterxml.jackson.core.JsonFactory
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import org.apache.http.entity.mime.MIME.UTF8_CHARSET
import org.bouncycastle.crypto.tls.ConnectionEnd.client
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.charset.Charset
import java.util.*


class CMakeDebuggerProxy(debugPort: Int) : CMakeDebuggerListenerHub() {
    private val selector = Selector.open()
    private val myPort: Int = debugPort
    private val hostAddress = InetSocketAddress("localhost", myPort)
    private var _client: SocketChannel? = null

    fun getClient() : SocketChannel {
        initClient()
        return _client!!
    }
    private var stack = CMakeExecutionStack(this)
    private val jsonFactory = JsonFactory()
    private var evalCallbacks = HashMap<String, XDebuggerEvaluator.XEvaluationCallback>()
    private var isRunning = true

    fun GetLastBacktrace() : CMakeExecutionStack =
            stack


    fun startClientThread() : Boolean {
        if(!initClient())
            return false
        val t = object : Thread() {
            override fun run() {
                startClient()
            }
        }
        t.start()
        return true
    }

    fun initClient() : Boolean {
        try {
            if (_client == null)
                _client = SocketChannel.open(hostAddress)
        } catch (e: Exception) {
            isRunning = false
            return false
        }
        isRunning = true
        return true;
    }

    fun startClient() : Boolean {
        if(!initClient())
            return false

        val client = this.getClient()
        client.configureBlocking(false)
        client.register(this.selector, SelectionKey.OP_READ)
        while (isRunning) {
            try {

                // wait for events
                this.selector.select()

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
                println(e)
                e.printStackTrace()
                readBuffer = ""
                bracesDepth = 0
            }
        }
        client.shutdownInput()
        client.shutdownOutput()
        return true
    }

    var bracesDepth = 0
    var readBuffer = ""
    private fun OnRead(data : String) {
        for( c : Char in data) {
            if(c == '{') bracesDepth++
            if(c == '}') bracesDepth--
            readBuffer += c
            if(bracesDepth == 0) {
                processMessage(readBuffer)
                readBuffer = ""
            }
        }
    }

    private fun processMessage(readBuffer: String) {
        println("New message: " + readBuffer)
        val parser = JsonParser()
        val json = parser.parse(readBuffer)
        if(json.isJsonObject) {
            var obj = json.asJsonObject!!
            if(obj["State"] != null && obj["State"].isJsonPrimitive()) {

                var pos = computeStackFromJson(obj)
                OnStateChange(obj["State"].asString, pos?.position?.File ?: "", pos?.position?.line ?: 0)
            }
            else if(obj["Request"] != null && obj["Request"].isJsonPrimitive()) {
                if(evalCallbacks[ obj["Request"].asString ] != null) {
                    if (obj["Response"].isJsonPrimitive)
                        evalCallbacks[obj["Request"].asString]?.evaluated(CMakeValue(this, obj["Response"].asString))
                    else
                        evalCallbacks[obj["Request"].asString]?.errorOccurred("Expression doesn't evaluate.")
                    evalCallbacks.remove(obj["Request"].asString)
                }

            }
        }
    }

    private fun  computeStackFromJson(obj: JsonObject) : CMakeStackFrame? {
        var stack = ArrayList<CMakeStackFrame>()
        var current : CMakeStackFrame? = null

        var bt = obj["Backtrace"]
        if(bt != null && bt.isJsonArray) {
            var arr = bt.asJsonArray
            for (i in arr) {
                val item = i.asJsonObject
                var stackFrame = CMakeStackFrame(this, SourceFilePosition(item["Line"].asInt - 1, item["File"].asString), item["Name"].asString)
                if(current == null)
                    current = stackFrame
                stack.add(stackFrame)
            }
            this.stack = CMakeExecutionStack(this, stack)
        }
        return current
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
        OnRead(String(data, UTF8_CHARSET))
    }

    fun pause() = sendCommand("Break")
    fun resume() = sendCommand("Continue")
    fun stepOut() = sendCommand( "StepOut" )
    fun stepInto() = sendCommand( "StepIn" )
    fun stepOver() = sendCommand( "StepOver" )

    private fun sendCommand(cmd : Map<String, out Any>) {
        var obj = JsonObject()
        for(k in cmd)
            if(k.value is Int)
                obj.addProperty(k.key, k.value as Int)
            else if(k.value is String)
                obj.addProperty(k.key, k.value as String)

        sendString(obj.toString())
    }
    private fun sendString(cmd: String) : Boolean {
        getClient().write(ByteBuffer.wrap(cmd.toByteArray(Charset.forName("UTF-8"))))
        println("Send command: " + cmd)
        return true
    }
    private fun sendCommand(cmd: String) {
        var cmd = "{ \"Command\": \""+ cmd + "\" }"
        sendString(cmd)
    }

    fun  connect(indicator: ProgressIndicator, serverProcessHandler: OSProcessHandler, times: Int) {
        serverProcessHandler.startNotify()
        startClientThread()
        resume()
    }
    fun shutdown() {
        isRunning = false
    }

    fun  isReady(): Boolean = isRunning

    fun  removeBreakPoint(sourceFile: SourceFilePosition) =
            sendCommand( mapOf("Command" to "RemoveBreakpoint",
            "File" to sourceFile.File,
            "Line" to (sourceFile.myLine + 1)
    ) )

    fun  addBreakPoint(sourceFile: SourceFilePosition) =
            sendCommand( mapOf("Command" to "AddBreakpoint",
            "File" to sourceFile.File,
            "Line" to (sourceFile.myLine + 1)
            ) )

    fun  evaluate(str: String, cb: XDebuggerEvaluator.XEvaluationCallback, location: XSourcePosition?) {
        evalCallbacks[str] = cb
        sendCommand( mapOf("Command" to "Evaluate", "Request" to str))
    }
}

