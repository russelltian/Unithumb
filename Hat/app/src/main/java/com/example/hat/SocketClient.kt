package com.example.hat
import android.annotation.SuppressLint
import android.util.Log
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
    private var client: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
    private var hostAddress: InetSocketAddress? = null
    private var future: Future<Void>? = null
    //called when starting the app
    @SuppressLint("LogNotTimber")
    fun setup(address: String, port: Int){
        thread{
            hostAddress = InetSocketAddress(address, port)
            Log.i("SocketClient","Set up listening server at $address on port $port")
        }

    }

    // check if the socket channel is connected
    private fun connected(): Boolean {
        if (future == null) return false
        return (future!!.get(100, TimeUnit.MILLISECONDS) == null)
    }

    @SuppressLint("LogNotTimber")
    fun connect(){
        thread{
            try {
                Log.i("SocketClient","Socket connecting ......")
                if (future == null){
                    future = client.connect(hostAddress)
                }
                var counter = 10
                while(!connected() && counter > 0){
                    future = client.connect(hostAddress)
                    counter -= 1
                }
                if (!connected()){
                    Log.e("SocketClient","Assertion failed, the socket failed to connect")
                }else{
                    Log.i("SocketClient","Socket is connected")
                }
            } catch (e:Exception){
                Log.e("SocketClient","Socket failed to connect : $e")
            }
        }
    }

    @SuppressLint("LogNotTimber")
    fun sendMessage(message: String){
       thread{
            try {
                if (!connected()) {
                    Log.i("SocketClient send message","Socket not connected, trying to connect")
                    connect()
                }
                client.write(ByteBuffer.wrap(message.toByteArray()))
            } catch (e:Exception){
                Log.e("SocketClient send message","Socket failed to connect : $e")
            }
       }
    }

    fun close(){
        client.close()
        println("Close connection")
    }
}