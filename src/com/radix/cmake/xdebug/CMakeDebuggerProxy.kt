package com.radix.cmake.xdebug

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.intellij.openapi.project.Project
import org.apache.http.entity.mime.MIME.UTF8_CHARSET
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel


class CMakeDebuggerProxy(project: Project, debugPort: Int) {
    private val selector = Selector.open()
    private val myProject: Project = project
    private val myPort: Int = debugPort
    val jsonFactory = JsonFactory()

    fun startClientThread() {
        val t = object : Thread() {
            override fun run() {
                startClient()
            }
        }
        t.start()
    }
    fun startClient() {
        val hostAddress = InetSocketAddress("localhost", myPort)
        val client = SocketChannel.open(hostAddress)
        client.configureBlocking(false)
            client.register(this.selector, SelectionKey.OP_READ)

        while (true) {
            // wait for events
            this.selector.select()

            //work on selected keys
            val keys = this.selector.selectedKeys().iterator()
            while (keys.hasNext()) {

                // this is necessary to prevent the same key from coming up
                // again the next time around.
                keys.remove()

                if (!keys.next().isValid) {
                    continue
                }

                if (keys.next().isReadable) {
                    this.read(keys.next())
                }

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
            if(bracesDepth == 0)
                processMessage(readBuffer)
            readBuffer = ""
        }
    }

    private fun processMessage(readBuffer: String) {
        var parser = jsonFactory.createParser(readBuffer)

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

}