package com.example.hat

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class DeviceConnectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_connection)

        findViewById<View>(R.id.device_connection_return).setOnClickListener{
            val intent = Intent(this, MapNavigationActivity::class.java).apply{
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.device_connection_bluetooth_setting).setOnClickListener{
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val cn = ComponentName(
                "com.android.settings",
                "com.android.settings.bluetooth.BluetoothSettings"
            )
            intent.component = cn
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}