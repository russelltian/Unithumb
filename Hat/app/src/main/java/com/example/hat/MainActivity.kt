package com.example.hat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SocketInstance.setup(address = "192.168.0.27", port = 8001)
        //SocketInstance.setup(address = "10.0.2.2", port = 8001)
    }

    fun switchToNavigationPage(view: View) {
        findViewById<Button>(R.id.getmap)
        val intent = Intent(this,MapNavigationActivity::class.java).apply{
            println("Main page: Going to Navigation Page")
        }
        startActivity(intent)
    }

}