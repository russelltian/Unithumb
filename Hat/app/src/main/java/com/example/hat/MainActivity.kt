package com.example.hat

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style

class MainActivity : AppCompatActivity() {
    private var mapView: MapView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))


//        SocketInstance.setup(address = "10.0.2.2", port = 8001)
        SocketInstance.setup(address = "192.168.0.29", port = 8000)
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
//                view ->Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Connect", createSocketClient()).show()
            setContentView(R.layout.map_navigation)
            mapView = findViewById(R.id.mapView)
            mapView?.onCreate(savedInstanceState)
            mapView?.getMapAsync { mapboxMap ->
                mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                    // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                }
            }
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