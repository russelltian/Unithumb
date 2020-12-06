package com.example.hat

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.hat.androidbluetoothserial.BluetoothManager
import com.example.hat.androidbluetoothserial.BluetoothSerialDevice
import com.example.hat.androidbluetoothserial.SimpleBluetoothDeviceInterface
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.StrictMath.*
import java.text.DecimalFormat
//import com.example.hat.androidbluetoothserial

/*
This is the main activity of the app.
 */
class MapNavigationActivity: AppCompatActivity(),OnMapReadyCallback,PermissionsListener,ProgressChangeListener

{
    private val REQUEST_CODE_AUTOCOMPLETE = 1
    private var mapView: MapView? = null
    private var map: MapboxMap?= null
    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    //navigation
    private var navigationMapRoute: NavigationMapRoute? = null
//    private var route: DirectionsRoute ?= null
    private var globalDestination: Point ?= null
    //annotation
    private var symbolManager:SymbolManager?=null


    private var mapboxNavigation:MapboxNavigation ?=null
//    private val locationEngine = ReplayRouteLocationEngine()
    private val routeManager = RouteManager(this)

    // Bluetooth
    // Setup our BluetoothManager
    private var bluetoothManager: BluetoothManager? = null
    private val macaddress = "98:D3:37:90:E4:A9"
    private var deviceInterface: SimpleBluetoothDeviceInterface? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Map access token is configured here.
        // TODO: the mapbox access token should be stored on a file for all copy of the program
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        mapboxNavigation = MapboxNavigation(this, getString(R.string.mapbox_access_token))
        mapboxNavigation!!.addProgressChangeListener(this)
        // This contains the MapView in XML and needs to be called after getting access token
        setContentView(R.layout.activity_map_navigation)

        // Deal with off route situation, reroute the path
        mapboxNavigation!!.addOffRouteListener{
            if (globalDestination != null){
                symbolManager?.deleteAll()
                map?.locationComponent?.lastKnownLocation?.longitude?.let { map?.locationComponent?.lastKnownLocation?.latitude?.let { it1 ->
                    Point.fromLngLat(it,
                        it1
                    )
                } }?.let {
                    routeManager.getRoute(it, globalDestination!!)
                }
            }
        }
        // Create view on the app and set it up in "onMapReady"
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        // Bluetooth Set up listener
        findViewById<View>(R.id.searchDeviceButton).setOnClickListener{
            //initBlueTooth()
        }
//        findViewById<View>(R.id.disconnect).setOnClickListener{
//            disconnect()
//        }
        // TODO not sure if we keep this or not
//        this.findViewById<Button>(R.id.start_navigating).setOnClickListener{
//            if (routeManager.route == null){
//                return@setOnClickListener
//            }
////            locationEngine.assign(routeManager.route)
////            mapboxNavigation?.locationEngine = locationEngine
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return@setOnClickListener
//            }
//            map?.locationComponent?.isLocationComponentEnabled = true
//         //   mapboxNavigation?.startNavigation(route!!)
//
//        }

        testBearing()

    }


    override fun onMapReady(mapboxMap: MapboxMap) {
        this.map = mapboxMap
        // By long pressing the map, we get a new route with current position as origin and long press location as destination
        mapboxMap.addOnMapLongClickListener{click->
            val origin =
                map?.locationComponent?.lastKnownLocation?.longitude?.let { map?.locationComponent?.lastKnownLocation?.latitude?.let { it1 ->
                    Point.fromLngLat(it,
                        it1
                    )
                } }
            val destination: Point =
                Point.fromLngLat(click.longitude,click.latitude)
            globalDestination = destination
            origin?.let { routeManager.getRoute(it, globalDestination!!) }
            symbolManager?.deleteAll()
            // TODO: fix icon name with proper lib
            symbolManager?.create(
                SymbolOptions()
                    .withLatLng(LatLng(click.latitude,click.longitude))
                    .withIconImage("666")
                    .withIconSize(2.0f)
            )
            // the other trigger will not be skipped
            false
        }
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {style->
            initSearchFab()
            // Map is set up and the style has been loaded.
            enableLocationComponent(style)
            symbolManager = mapView?.let { SymbolManager(it,mapboxMap, style) }
            // set non-data-driven properties, such as:
            symbolManager?.iconAllowOverlap = true
            symbolManager?.iconIgnorePlacement = true
            symbolManager?.iconTranslate = arrayOf(-4f, 5f)
            symbolManager?.iconRotationAlignment = ICON_ROTATION_ALIGNMENT_VIEWPORT
            ResourcesCompat.getDrawable(resources, R.drawable.map_default_map_marker,null)?.let {
                style.addImage("666",
                    it
                )
            }
        }
    }

    /* ----------------------------------------------------------------------

                             Route Searching
     A searching bar on the map for destination searching
    */
    private fun initSearchFab() {
        findViewById<View>(R.id.searchRouteButton).setOnClickListener {
            val intent: Intent = (getString(
                R.string.mapbox_access_token
            ).let { it1 ->
                PlaceAutocomplete.IntentBuilder()
                    .accessToken(
                        it1
                    )
                    .placeOptions(
                        PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#EEEEEE"))
                            .limit(5)
                            .build(PlaceOptions.MODE_CARDS)
                    )
                    .build(this)
            }) as Intent
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE)
        }
    }


    @SuppressLint("LogNotTimber", "CheckResult")
    override fun onProgressChange(location: Location?, routeProgress: RouteProgress?) {
        Log.i("route progress", routeProgress?.currentState().toString())
        if(routeManager.route != null && routeProgress != null && location != null){

            val origin = location.longitude.let { Point.fromLngLat(it,location.latitude) }

            val nextPoint: Point? = routeManager.getNextPoint(origin)
            if (nextPoint != null) {
                symbolManager?.create(SymbolOptions()
                    .withLatLng(LatLng(nextPoint.latitude(),nextPoint.longitude()))
                    .withIconImage("666")
                    .withIconSize(0.8f)
                )
                val sendDegree = calcBearing(origin, nextPoint)
                Log.i("OnProgressChange", sendDegree)
                // Send message to device connected
                if (this.deviceInterface != null && bluetoothManager!=null){
                    this.deviceInterface!!.sendMessage(sendDegree)
                }
                else{
//                    initBlueTooth()
                }

            }
        }

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

// Retrieve selected location's CarmenFeature
            val selectedCarmenFeature: CarmenFeature = PlaceAutocomplete.getPlace(data)

// Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
// Then retrieve and update the source designated for showing a selected location's symbol layer icon
            if (map != null) {
                val style: Style? = map!!.style
                if (style != null) {
                    style.getSourceAs<GeoJsonSource?>("geojsonSourceLayerId")?.setGeoJson(
                        FeatureCollection.fromFeatures(
                            arrayOf<Feature>(
                                Feature.fromJson(
                                    selectedCarmenFeature.toJson()
                                )
                            )
                        )
                    )

// Move map camera to the selected location
                    map!!.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(
                                    LatLng(
                                        (selectedCarmenFeature.geometry() as Point).latitude(),
                                        (selectedCarmenFeature.geometry() as Point).longitude()
                                    )
                                )
                                .zoom(14.0)
                                .build()
                        ), 4000
                    )
                    //todo: suplicated call, maybe swtich to global latter
                    val origin =
                        map?.locationComponent?.lastKnownLocation?.longitude?.let { map?.locationComponent?.lastKnownLocation?.latitude?.let { it1 ->
                            Point.fromLngLat(it,
                                it1
                            )
                        } }
                    globalDestination = selectedCarmenFeature.geometry() as Point
                    origin?.let { routeManager.getRoute(it, globalDestination!!)}
                }
            }
        }
    }

    fun displayRoute(){
        routeManager.route?.let { mapboxNavigation?.startNavigation(it) }

        if(navigationMapRoute == null){
            navigationMapRoute = mapView?.let { it1 ->
                map?.let { it2 ->
                    NavigationMapRoute(mapboxNavigation,
                        it1, it2
                    )
                }
            }

        }else{
            navigationMapRoute?.updateRouteVisibilityTo(false)
            navigationMapRoute?.updateRouteArrowVisibilityTo(false)
            navigationMapRoute?.addRoute(routeManager.route)
        }

    }



    /*

    ------------------------------------Built in location service-----------------------------------

     */
    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            // Get an instance of the LocationComponent and then adjust its settings
            map?.locationComponent?.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    // When the user deny the permission for the first time
    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        // Present a toast or a dialog explaining why they need to grant permission
        Toast.makeText(this,"The location request has been declined by the user", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if(granted){
            enableLocationComponent(map?.style!!)
        }else{
            Toast.makeText(this,"User location permission is not granted",Toast.LENGTH_LONG).show()
            finish()
        }
    }


    override fun onStart(){
        super.onStart()
        mapView?.onStart()
    }
    override fun onResume(){
        super.onResume()
        mapView?.onResume()
    }
    override fun onPause(){
        super.onPause()
        mapView?.onPause()
    }
    override fun onStop(){
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroy(){
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    fun calcBearing(origin: Point,nextPoint: Point): String {
//        val lat1 = origin.latitude() * Math.PI/180
//        val lon1 = origin.longitude() * Math.PI/180
//        val lat2 = nextPoint.latitude() * Math.PI/180
//        val lon2 = nextPoint.longitude() * Math.PI/180
//        val dLon = (lon2 - lon1) * Math.PI/180
        val bearingX = cos(nextPoint.latitude()*Math.PI/180)* sin(
            ((nextPoint.longitude() - origin.longitude())
                    * Math.PI / 180)
        )
        val bearingY = cos(origin.latitude()*Math.PI/180)*sin(nextPoint.latitude()*Math.PI/180) -
                sin(origin.latitude()*Math.PI/180)*cos(nextPoint.latitude()*Math.PI/180)*
                cos((nextPoint.longitude()-origin.longitude())*Math.PI/180)
//        val bearingX = cos(lat2) * sin(dLon)
//        val bearingY = cos(lat1)*sin(lat2) - sin(lat1)*cos(lat2)*cos(dLon)
        val bearing = atan2(bearingX,bearingY) * 180 / Math.PI
        //bearing = (360 - ((bearing+360)%360))
        val format = DecimalFormat("#.##")
        return format.format(bearing)
    }

    fun testBearing(){
        val a = calcBearing(Point.fromLngLat( -94.581213,39.099912),
            Point.fromLngLat( -90.200203,38.627089))
        val b = calcBearing(Point.fromLngLat( -79.3832,43.6532),
            Point.fromLngLat( -79.383,43.6531))

        print(a)
    }

    /*
 ---------------------------------Bluetooth-----------------------------------------------------
       Set up bluetooth scan
     https://github.com/Polidea/RxAndroidBle
 */

    @SuppressLint("CheckResult", "LogNotTimber")
    private fun initBlueTooth(){
        bluetoothManager = BluetoothManager.instance
        if (bluetoothManager == null){
            Toast.makeText(this, "Bluetooth not available.", Toast.LENGTH_LONG).show() // Replace
            finish()
        }

        val pairedDevices: Collection<BluetoothDevice> = bluetoothManager!!.pairedDevicesList
        for (device in pairedDevices) {
            Log.d("bluetooth", "Device name: " + device.name)
            Log.d("bluetooth", "Device MAC Address: " + device.address)
        }
        connectDevice(macaddress)

    }


    @SuppressLint("CheckResult")
    private fun connectDevice(mac: String) {
        bluetoothManager!!.openSerialDevice(mac)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onConnected, this::onError)
    }

    private fun onConnected(connectedDevice: BluetoothSerialDevice) {
        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        this.deviceInterface = connectedDevice.toSimpleDeviceInterface()

        // Listen to bluetooth events
//        deviceInterface!!.setListeners(
//            { message: String -> onMessageReceived(message) },
//            { message: String -> onMessageSent(message) },
//            { error: Throwable -> onError(error) }
//        )

        this.deviceInterface!!.setMessageReceivedListener( object:SimpleBluetoothDeviceInterface.OnMessageReceivedListener  {
            override fun onMessageReceived(message: String){
//                val text_box = findViewById<TextView>(R.id.debug_data_received)
//                text_box.setText(message).toString()
            }
        })

        this.deviceInterface!!.setMessageSentListener( object:SimpleBluetoothDeviceInterface.OnMessageSentListener  {
            @SuppressLint("LogNotTimber")
            override fun onMessageSent(message: String) {
                Log.d("bluetooth", "Sent a message! Message was: $message")
            }
        })

        this.deviceInterface!!.setErrorListener( object:SimpleBluetoothDeviceInterface.OnErrorListener  {
            @SuppressLint("LogNotTimber")
            override fun onError(error: Throwable) {
                Log.e("bluetooth",error.toString())
                bluetoothManager?.close()
                bluetoothManager = null
                deviceInterface = null
            }
        })
    }

    private fun disconnect(){
        // Disconnect all devices
        bluetoothManager?.closeDevice(macaddress) // Close by mac
        bluetoothManager?.close()
        bluetoothManager = null
        deviceInterface = null
    }


    @SuppressLint("LogNotTimber")
    private fun onError(error: Throwable) {
        // Handle the error
        Log.e("bluetooth",error.toString())
        bluetoothManager?.close()
        bluetoothManager = null
        deviceInterface = null
    }
}

