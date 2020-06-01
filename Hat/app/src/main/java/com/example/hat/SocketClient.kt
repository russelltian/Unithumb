package com.example.hat
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Future
import kotlin.concurrent.thread

class SocketClient(){
    var client: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
    var connected: Boolean = false
    fun run(address: String, port: Int){
        val hostAddress = InetSocketAddress(address, port)

        try {
            if (!connected) {
                val future = client.connect(hostAddress)
                future.get() // should return NULL
                connected = true;
            }
            if (BuildConfig.DEBUG && !client.isOpen()) {
                error("Assertion failed, the connection socket is not opened")
            }
            val msg = "hello I am the phone"
            client.write(ByteBuffer.wrap(msg.toByteArray()))
        } catch (e:Exception){
                println("Socket failed to connect : $e")
        }
    }


}
class SocketClient1(address: String, port: Int){
    private val connection: Socket = Socket(address, port)
    private var connected: Boolean = true

    init {
        println("Connected to server at $address on port $port")
    }

    private val reader: Scanner = Scanner(connection.getInputStream())
    private val writer: OutputStream = connection.getOutputStream()

    fun run() {
        thread { read() }
        while (connected) {
            val input = readLine() ?: ""
            if ("exit" in input) {
                connected = false
                reader.close()
                connection.close()
            } else {
                write(input)
            }
        }

    }

    private fun write(message: String) {
        writer.write((message + '\n').toByteArray(Charset.defaultCharset()))
    }

    private fun read() {
        while (connected)
            println(reader.nextLine())
    }
}