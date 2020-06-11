package com.example.hat

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
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
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapNavigationActivity: AppCompatActivity(),OnMapReadyCallback,PermissionsListener
{
    private var mapView: MapView? = null
    private var map: MapboxMap?= null
    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    private var startButton: Button?=null
    //navigation
    private var navigationMapRoute: NavigationMapRoute? = null
    private var route: DirectionsRoute ?= null
    //annotation
    private var symbolManager:SymbolManager?=null

    val mapRouteProgressChangeListener = object: ProgressChangeListener{
        override fun onProgressChange(location: Location?, routeProgress: RouteProgress?) {
            if (routeProgress != null) {
                Log.i("onProgressChange", "onProgressChange" + routeProgress.legIndex())
            }
        }
    }

    private var mapboxNavigation:MapboxNavigation ?=null


    //location
//    private var origin:Point?= null
//    private var destination:Point?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Map access token is configured here.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        mapboxNavigation = MapboxNavigation(this, getString(R.string.mapbox_access_token))
        mapboxNavigation!!.addProgressChangeListener(mapRouteProgressChangeListener)

        // This contains the MapView in XML and needs to be called after getting access token
        setContentView(R.layout.activity_map_navigation)



        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        this.findViewById<Button>(R.id.start_navigating).setOnClickListener{
            if (route == null){
                return@setOnClickListener
            }

//            val options = NavigationLauncherOptions.builder()
//                .directionsRoute(route)
//                .shouldSimulateRoute(true)
//                .build()
//            NavigationLauncher.startNavigation(this,options)
        }



    }

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
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.map = mapboxMap
        mapboxMap.addOnMapLongClickListener{click->

            val origin =
                map?.locationComponent?.lastKnownLocation?.longitude?.let { map?.locationComponent?.lastKnownLocation?.latitude?.let { it1 ->
                    Point.fromLngLat(it,
                        it1
                    )
                } }
            val destination: Point =
                Point.fromLngLat(click.longitude,click.latitude)

            if (origin != null) {
                getRoute(origin,destination)
            }
            symbolManager?.deleteAll()
            symbolManager?.create(
                SymbolOptions()
                    .withLatLng(LatLng(click.latitude,click.longitude))
                    .withIconImage("666")
                    .withIconSize(2.0f)
            )
            false
        }
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {style->
            // Madp is set up and the style has loaded. Now you can add data or make other map adjustments
            enableLocationComponent(style)
            //symbolManager = mapView?.let { SymbolManager(it,mapboxMap,style) }
            symbolManager = mapView?.let { SymbolManager(it,mapboxMap, style) }
            // set non-data-driven properties, such as:
            symbolManager?.iconAllowOverlap = true
            symbolManager?.iconIgnorePlacement = true
            symbolManager?.iconTranslate = arrayOf(-4f, 5f)
            symbolManager?.iconRotationAlignment = ICON_ROTATION_ALIGNMENT_VIEWPORT
            style.addImage("666", IconFactory.getInstance(this).defaultMarker().bitmap)
        }
    }

    private fun getRoute(origin: Point, destination: Point){
        Mapbox.getAccessToken()?.let {
            NavigationRoute.builder(this)
                .accessToken(it)
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(object:Callback<DirectionsResponse>{
                    @SuppressLint("LogNotTimber")
                    override fun onResponse(
                        call: Call<DirectionsResponse>,
                        response: Response<DirectionsResponse>
                    ) {
                        val routeResponse = response
                        val body = routeResponse.body() ?:return

                        if (body.routes().count() == 0){
                            Log.e("MapNavigationActivity","No route found.")
                            return
                        }
                        if (navigationMapRoute != null){
                            navigationMapRoute?.updateRouteVisibilityTo(false)
                            navigationMapRoute?.updateRouteArrowVisibilityTo(false)
                        }else{
                            navigationMapRoute = mapView?.let { it1 ->
                                map?.let { it2 ->
                                    NavigationMapRoute(mapboxNavigation,
                                        it1, it2
                                    )
                                }
                            }
                        }
                        route = body.routes().first()
                        navigationMapRoute?.addRoute(body.routes().first())
                        mapboxNavigation?.startNavigation(body.routes().first())
//                        val mapboxNavigation = navigationMapRoute.get_mapboxNavigation
//                        mapboxNavigation.addProgressChangeListener()
//                        navigationMapRoute?.addProgressChangeListener()
                    }

                    @SuppressLint("LogNotTimber")
                    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                        Log.e("MapNavigationActivity","Error: ${t.message}")
                    }


                })
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    // When the user deny the permission for the first time
    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        // Present a toast or a dialog explaining why they need to grant permission
        Toast.makeText(this,"Meow", Toast.LENGTH_LONG).show()
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

}