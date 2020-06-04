package com.example.hat
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import kotlin.concurrent.thread
import java.net.InetSocketAddress as InetSocketAddress

/*
There should be only one socket object that connects with the hat

learn from this! https://examples.javacodegeeks.com/core-java/nio/channels/asynchronoussocketchannel/java-nio-channels-asynchronoussocketchannel-example/

 */
class SocketClient(address: String, port: Int){
    var client: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
    var connected: Boolean = false
    val hostAddress = InetSocketAddress(address, port)

    init {
        println("Connecting to server at $address on port $port")
    }

    fun run(){
        try {
            if (!connected) {
                val future = client.connect(hostAddress)
                future.get() // should return NULL
                connected = true
            }
            if (BuildConfig.DEBUG && !client.isOpen) {
                error("Assertion failed, the connection socket is not opened")
            }
            val msg = "hello I am the phone"
            client.write(ByteBuffer.wrap(msg.toByteArray()))
        } catch (e:Exception){
                println("Socket failed to connect : $e")
        }
    }
    fun sendMessage(message: String){
        thread{
            try {
                if (!connected) {
                    val future = client.connect(hostAddress)
                    future.get() // should return NULL
                    connected = true
                }
                if (BuildConfig.DEBUG && !client.isOpen) {
                    error("Assertion failed, the connection socket is not opened")
                }
                client.write(ByteBuffer.wrap(message.toByteArray()))
            } catch (e:Exception){
                println("Socket failed to connect : $e")
            }
        }
    }
    fun close(){
        client.close()
        connected = false
    }
}
/*
Socket instance that only has one copy in the program
 */
object SocketInstance {
    var client: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
    var connected:Boolean = false
    var hostAddress: InetSocketAddress? = null

    fun setup(address: String, port: Int){
        hostAddress = InetSocketAddress(address, port)
        println("Connecting to server at $address on port $port")
    }

    fun sendMessage(message: String){
        thread{
            try {
                if (!connected) {
                    print("Socket not connected yet")
                    val future = client.connect(hostAddress)
                    future.get() // should return NULL
                    connected = true
                }
                if (BuildConfig.DEBUG && !client.isOpen) {
                    error("Assertion failed, the connection socket is not opened")
                }
                client.write(ByteBuffer.wrap(message.toByteArray()))
            } catch (e:Exception){
                println("Socket failed to connect : $e")
            }
        }
    }
    fun close(){
        //client.close()
        println("Close connection")
        //connected = false
    }
}