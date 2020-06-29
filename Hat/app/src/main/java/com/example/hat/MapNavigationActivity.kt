package com.example.hat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
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
import com.mapbox.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import java.lang.StrictMath.*
import java.text.DecimalFormat

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
    private val locationEngine = ReplayRouteLocationEngine()
    private val routeManager = RouteManager(this)


    //location
//    private var origin:Point?= null
//    private var destination:Point?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Map access token is configured here.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        mapboxNavigation = MapboxNavigation(this, getString(R.string.mapbox_access_token))
        mapboxNavigation!!.addProgressChangeListener(this)
        // This contains the MapView in XML and needs to be called after getting access token
        setContentView(R.layout.activity_map_navigation)

        mapboxNavigation!!.addOffRouteListener{
            if (globalDestination != null){
                map?.locationComponent?.lastKnownLocation?.longitude?.let { map?.locationComponent?.lastKnownLocation?.latitude?.let { it1 ->
                    Point.fromLngLat(it,
                        it1
                    )
                } }?.let {
                    routeManager.getRoute(it, globalDestination!!, mapboxNavigation!!)
                    displayRoute()
                }
            }
        }
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        this.findViewById<Button>(R.id.start_navigating).setOnClickListener{
            if (routeManager.route == null){
                return@setOnClickListener
            }
            locationEngine.assign(routeManager.route)
            mapboxNavigation?.locationEngine = locationEngine
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@setOnClickListener
            }
            map?.locationComponent?.isLocationComponentEnabled = true
         //   mapboxNavigation?.startNavigation(route!!)

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
            globalDestination = destination
            origin?.let { routeManager.getRoute(it, globalDestination!!,mapboxNavigation!!) }
            displayRoute()
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
            initSearchFab()
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

    private fun initSearchFab() {
        findViewById<View>(R.id.fab_location_search).setOnClickListener {
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
                            .limit(10)
                            .build(PlaceOptions.MODE_CARDS)
                    )
                    .build(this)
            }) as Intent
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE)
        }
    }

//    private fun getRoute(origin: Point, destination: Point){
//        Mapbox.getAccessToken()?.let {
//            NavigationRoute.builder(this)
//                .accessToken(it)
//                .origin(origin)
//                .destination(destination)
//                //.profile(DirectionsCriteria.PROFILE_WALKING)
//                .build()
//                .getRoute(object:Callback<DirectionsResponse>{
//                    @SuppressLint("LogNotTimber")
//                    override fun onResponse(
//                        call: Call<DirectionsResponse>,
//                        response: Response<DirectionsResponse>
//                    ) {
//                        val body = response.body() ?:return
//
//                        if (body.routes().count() == 0){
//                            Log.e("MapNavigationActivity","No route found.")
//                            return
//                        }
//                        if (navigationMapRoute != null){
//                            navigationMapRoute?.updateRouteVisibilityTo(false)
//                            navigationMapRoute?.updateRouteArrowVisibilityTo(false)
//                        }else{
//                            navigationMapRoute = mapView?.let { it1 ->
//                                map?.let { it2 ->
//                                    NavigationMapRoute(mapboxNavigation,
//                                        it1, it2
//                                    )
//                                }
//                            }
//                            navigationMapRoute?.updateRouteVisibilityTo(true)
//                            navigationMapRoute?.updateRouteArrowVisibilityTo(true)
//                        }
//                        route = body.routes().first()
//                        navigationMapRoute?.addRoute(body.routes().first())
//
////                        val mapboxNavigation = navigationMapRoute.get_mapboxNavigation
////                        mapboxNavigation.addProgressChangeListener()
////                        navigationMapRoute?.addProgressChangeListener()
//                    }
//
//                    @SuppressLint("LogNotTimber")
//                    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
//                        Log.e("MapNavigationActivity","Error: ${t.message}")
//                    }
//
//
//                })
//        }
//    }

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

    @SuppressLint("LogNotTimber")
    override fun onProgressChange(location: Location?, routeProgress: RouteProgress?) {

        if(routeManager?.route != null && routeProgress != null && location != null){
            val nextPoint: Point? = routeManager.getNextPoint()
            val origin = location.longitude.let { Point.fromLngLat(it,location.latitude) }
            if (nextPoint != null) {
                symbolManager?.create(SymbolOptions()
                    .withLatLng(LatLng(nextPoint.latitude(),nextPoint.longitude()))
                    .withIconImage("666")
                    .withIconSize(0.4f)
                )
                val bearingX = cos(nextPoint.latitude())* sin(nextPoint.longitude()- (origin?.longitude())!!)
                val bearingY = cos(origin.latitude())*sin(nextPoint.latitude()) -
                        sin(origin.latitude())*cos(nextPoint.latitude())*cos(nextPoint.longitude()-origin.longitude())
                val bearing = atan2(bearingX,bearingY) * 180 / Math.PI
                val format = DecimalFormat("#.##")
                val sendDegree = format.format(bearing)
                Log.i("OnProgressChange", sendDegree)
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
                val style: Style? = map!!.getStyle()
                if (style != null) {
                    val source: GeoJsonSource? = style.getSourceAs("geojsonSourceLayerId")
                    if (source != null) {
                        source.setGeoJson(
                            FeatureCollection.fromFeatures(
                                arrayOf<Feature>(
                                    Feature.fromJson(
                                        selectedCarmenFeature.toJson()
                                    )
                                )
                            )
                        )
                    }

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
                    origin?.let { routeManager.getRoute(it, globalDestination!!,mapboxNavigation!!)}
                    displayRoute()
                }
            }
        }
    }

    private fun displayRoute(){
//        navigationMapRoute = mapView?.let { it1 ->
//            map?.let { it2 ->
//                NavigationMapRoute(mapboxNavigation,
//                    it1, it2
//                )
//            }
//        }
//        navigationMapRoute?.updateRouteVisibilityTo(true)
//        navigationMapRoute?.updateRouteArrowVisibilityTo(true)
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