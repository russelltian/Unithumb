package com.example.hat

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    //learn from this! https://examples.javacodegeeks.com/core-java/nio/channels/asynchronoussocketchannel/java-nio-channels-asynchronoussocketchannel-example/
    val client: AsynchronousSocketChannel = AsynchronousSocketChannel.open()

    fun createSocketClient(){
//         start a socket connection
//        var socket_obj = SocketClient("192.168.0.23",8000)
//        socket_obj.run()

        thread{
            val hostAddress = InetSocketAddress("10.0.2.2", 65432)
            try {
                val future = client.connect(hostAddress)
                future.get()
                val msg = "hello I am the phone"
                client.write(ByteBuffer.wrap(msg.toByteArray()))

            }catch (e:Exception){
                println("failed to connect")
            }

            val messages = arrayOf("Time goes fast.", "What now?", "Bye.")
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
//                view ->Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Connect", createSocketClient()).show()
            createSocketClient()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}