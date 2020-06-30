package com.example.hat
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import java.net.InetSocketAddress as InetSocketAddress

/*
Socket instance that only has one copy in the program
 */
object SocketInstance {
    var client: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
    var hostAddress: InetSocketAddress? = null
    var future: Future<Void>? = null
    //called when starting the app
    fun setup(address: String, port: Int){
        hostAddress = InetSocketAddress(address, port)
        println("Connecting to server at $address on port $port")
    }
    // check the channel is connected
    fun connected(): Boolean {
        if (future == null) return false
        return (future!!.get(100, TimeUnit.MILLISECONDS) == null)
    }

    fun connect(){
        try {
            println("Socket connecting ......")
            if (future == null){
                future = client.connect(hostAddress)
            }
            var counter = 10
            while(!connected()&& counter > 0){
                future = client.connect(hostAddress)
                counter -= 1
            }
            if (!connected()){
                error("Assertion failed, the socket failed to connect")
            }
                val future = client.connect(hostAddress)
                future.get() // should return NULL

            if (!client.isOpen) {
                error("Assertion failed, the connection socket is not opened")
            }
            println("Socket is connected")
        } catch (e:Exception){
            println("Socket failed to connect : $e")
        }
    }
    fun sendMessage(message: String){
       thread{
            try {
                if (!connected()) {
                    println("Socket not connected, trying connect")
                    connect()
                }
                client.write(ByteBuffer.wrap(message.toByteArray()))
            } catch (e:Exception){
                println("Socket failed to connect : $e")
            }
       }
    }
    fun close(){
        client.close()
        println("Close connection")
    }
}



// we don't use this one by far
/*
There should be only one socket object that connects with the hat

learn from this! https://examples.javacodegeeks.com/core-java/nio/channels/asynchronoussocketchannel/java-nio-channels-asynchronoussocketchannel-example/

 */
object SocketClient{
    var address = "10.0.2.2"
    var port = 8000
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
