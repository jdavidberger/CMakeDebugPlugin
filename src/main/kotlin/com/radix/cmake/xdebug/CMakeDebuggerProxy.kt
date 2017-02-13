package com.radix.cmake.xdebug

import com.fasterxml.jackson.core.JsonFactory
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessInfo
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


class CMakeDebuggerProxy(debugPort: Int) : CMakeDebuggerListenerHub(), JsonServerListener {
    var server = JsonServerWEvents(this)

    private var myPort: Int = debugPort
    private var hostAddress = InetSocketAddress("localhost", myPort)
    private var _client: SocketChannel? = null

    fun getClient() : SocketChannel {
        initClient()
        return _client!!
    }

    private var stack = CMakeExecutionStack(this)
    private val jsonFactory = JsonFactory()
    private var evalCallbacks = HashMap<String, XDebuggerEvaluator.XEvaluationCallback>()

    fun GetLastBacktrace() : CMakeExecutionStack =
            stack


    fun startClientThread() : Boolean {
        if(!initClient())
            return false

        server.startThread()
        return true
    }

    fun initClient() : Boolean {
        try {
            if (_client == null) {
                _client = SocketChannel.open(InetSocketAddress("localhost", myPort))
                server.addSocket(_client!!)
            }
        } catch (e: Exception) {
                server.isRunning = false
                return false
        }
        return true
    }

    override fun processMessage(key: SelectionKey, json: JsonElement) {
        if (json.isJsonObject) {
            var obj = json.asJsonObject!!
            println(obj.toString())
            if (obj["State"] != null && obj["State"].isJsonPrimitive()) {
                var pos = computeStackFromJson(obj)
                OnStateChange(obj["State"].asString, pos?.position?.File ?: "", pos?.position?.line ?: 0)
            } else if (obj["Request"] != null && obj["Request"].isJsonPrimitive()) {
                if (evalCallbacks[obj["Request"].asString] != null) {
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

    fun pause() = sendCommand("Break")
    fun resume() = sendCommand("Continue")
    fun stepOut() = sendCommand( "StepOut" )
    fun stepInto() = sendCommand( "StepIn" )
    fun stepOver() = sendCommand( "StepOver" )

    private fun sendCommand(cmd : Map<String, Any>) {
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
    private fun sendCommand(cmdId: String) {
        var cmd = "{ \"Command\": \""+ cmdId + "\" }"
        sendString(cmd)
    }

    fun  connect(indicator: ProgressIndicator, serverProcessHandler: OSProcessHandler, times: Int) {
        serverProcessHandler.startNotify()
        startClientThread()
    }
    fun  connect(indicator: ProgressIndicator, times: Int) {
        startClientThread()
        pause()
    }

    fun shutdown() {
        server.isRunning = false
    }

    fun  isReady(): Boolean = server.isRunning

    fun  removeBreakPoint(sourceFile: SourceFilePosition) =
            sendCommand( mapOf("Command" to "RemoveBreakpoint",
            "File" to sourceFile.File.replace("\\", "/"),
            "Line" to (sourceFile.myLine + 1)
    ) )

    fun  addBreakPoint(sourceFile: SourceFilePosition) =
            sendCommand( mapOf("Command" to "AddBreakpoint",
            "File" to sourceFile.File.replace("\\", "/"),
            "Line" to (sourceFile.myLine + 1)
            ) )

    fun  evaluate(str: String, cb: XDebuggerEvaluator.XEvaluationCallback, location: XSourcePosition?) {
        evalCallbacks[str] = cb
        sendCommand( mapOf("Command" to "Evaluate", "Request" to str))
    }

    constructor(client: SocketChannel) : this(0) {
        _client = client
        server.addSocket(_client!!)
    }
}

