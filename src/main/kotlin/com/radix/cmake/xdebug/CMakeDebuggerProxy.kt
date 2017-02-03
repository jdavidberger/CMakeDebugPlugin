package com.radix.cmake.xdebug

import com.fasterxml.jackson.core.JsonFactory
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import org.apache.http.entity.mime.MIME.UTF8_CHARSET
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.charset.Charset
import java.util.*


interface CMakeDebuggerListener {
    fun OnStateChange(newState: String, file: String, line: Int)
}

open class CMakeDebuggerListenerHub : CMakeDebuggerListener {
    var listeners = ArrayList<CMakeDebuggerListener>()

    fun AddListener(listener : CMakeDebuggerListener) {
        listeners.add(listener)
    }

    override fun OnStateChange(newState: String, file: String, line: Int) {
        for (obj in listeners) {
            obj.OnStateChange(newState, file, line)
        }
    }

}

class CMakeDebuggerProxy(debugPort: Int) : CMakeDebuggerListenerHub() {
    private val selector = Selector.open()
    //private val myProject: Project = project
    private val myPort: Int = debugPort
    val hostAddress = InetSocketAddress("localhost", myPort)
    val client = SocketChannel.open(hostAddress)
    var stack = CMakeExecutionStack(this)
    val jsonFactory = JsonFactory()
    var evalCallbacks = HashMap<String, XDebuggerEvaluator.XEvaluationCallback>()

    fun GetLastBacktrace() : CMakeExecutionStack {
        return stack
    }

    fun startClientThread() {
        val t = object : Thread() {
            override fun run() {
                startClient()
            }
        }
        t.start()
    }
    fun startClient() {
        client.configureBlocking(false)
        client.register(this.selector, SelectionKey.OP_READ)

        while (true) {
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
            }
        }
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


        var parser = JsonParser()
        var json = parser.parse(readBuffer)
        if(json.isJsonObject) {

            var obj = json.asJsonObject!!
            if(obj["State"] != null && obj["State"].isJsonPrimitive()) {
                computeStackFromJson(obj)
                OnStateChange(obj["State"].asString, obj["File"].asString, obj["Line"].asInt - 1)
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

    private fun  computeStackFromJson(obj: JsonObject) {
        var stack = ArrayList<CMakeStackFrame>()
        var bt = obj["Backtrace"]
        if(bt != null && bt.isJsonArray) {
            var arr = bt.asJsonArray
            for (i in arr) {
                val item = i.asJsonObject
                stack.add(CMakeStackFrame(this, SourceFilePosition(  item["Line"].asInt - 1, item["File"].asString), item["Name"].asString))
            }
            this.stack = CMakeExecutionStack(this, stack)
        }
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

    fun step() {
        sendCommand("Step")
    }

    fun pause() {
        sendCommand("Break")
    }

    private fun sendCommand(cmd : Map<String, out Any>) {
        var obj = JsonObject()
        for(k in cmd)
            if(k.value is Int)
                obj.addProperty(k.key, k.value as Int)
            else if(k.value is String)
                obj.addProperty(k.key, k.value as String)

        sendString(obj.toString())
    }
    private fun sendString(cmd: String) {
        client.write(ByteBuffer.wrap(cmd.toByteArray(Charset.forName("UTF-8"))))
        println("Send command: " + cmd)
    }
    private fun sendCommand(cmd: String) {
        var cmd = "{ \"Command\": \""+ cmd + "\" }"
        return sendString(cmd)
    }

    fun resume() {
        sendCommand("Continue")
    }

    fun  connect(indicator: ProgressIndicator, serverProcessHandler: OSProcessHandler, times: Int) {
        serverProcessHandler.startNotify()
        startClientThread()
    }

    fun  isReady(): Boolean {
        return true
    }
    fun  attach(sourceFiles: Set<SourceFilePosition>) {
        for (position in sourceFiles) {
            addBreakPoint(position)
        }
        resume()
    }

    fun  removeBreakPoint(sourceFile: SourceFilePosition) {
        sendCommand( mapOf("Command" to "RemoveBreakpoint",
                "File" to sourceFile.File,
                "Line" to (sourceFile.myLine + 1)
        ) )
    }
    fun  addBreakPoint(sourceFile: SourceFilePosition) {
        sendCommand( mapOf("Command" to "AddBreakpoint",
                "File" to sourceFile.File,
                "Line" to (sourceFile.myLine + 1)
                ) )
    }

    fun  evaluate(str: String, cb: XDebuggerEvaluator.XEvaluationCallback, location: XSourcePosition?) {
        evalCallbacks[str] = cb
        sendCommand( mapOf("Command" to "Evaluate", "Request" to str))
    }

    fun stepOut() {
        sendCommand( "StepOut" )
    }
    fun stepInto() {
        sendCommand( "StepIn" )
    }
    fun stepOver() {
        sendCommand( "StepOver" )
    }
}

class CMakeValue(debugger: CMakeDebuggerProxy, v: String) : XValue() {
    var value = v
    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        node.setPresentation(null, "",  value, false)
    }
}
