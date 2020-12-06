package com.example.hat

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.mapbox.turf.TurfMeasurement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class RouteManager(context: MapNavigationActivity) {
    private val ctx = context
    var route: DirectionsRoute?= null
    private var geometry: Queue<Point>?=null
    private var nextPoint: Point ?= null

    @SuppressLint("LogNotTimber")
    fun getNextPoint(curr:Point): Point? {
        if(nextPoint==null){
            if(geometry==null){
                Log.e("Route manager:","empty geometry in the route returned")
            }else{
                nextPoint = geometry!!.poll()
            }
        }else{
            // David's codes
            var last = nextPoint?.latitude()?.div(10)?.let { Point.fromLngLat(
                nextPoint?.longitude()?.div(10)!!,it)
            }
            var distance = TurfMeasurement.distance(curr,last!!)
            Log.i("distance:",distance.toString())
            while(distance <0.02){
                if(!geometry?.isEmpty()!!){
                    nextPoint = geometry!!.poll()
                }else{
                    break
                }
                last = nextPoint?.latitude()?.div(10)?.let { Point.fromLngLat(
                    nextPoint?.longitude()?.div(10)!!,it)
                }
                distance = TurfMeasurement.distance(curr,last!!)
            }

        }

        return nextPoint?.latitude()?.div(10)?.let { Point.fromLngLat(
            nextPoint?.longitude()?.div(10)!!,it)
        }
    }

    fun getRoute(origin: Point, destination: Point){
        Mapbox.getAccessToken()?.let {
            NavigationRoute.builder(ctx)
                .accessToken(it)
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(object : Callback<DirectionsResponse> {
                    @SuppressLint("LogNotTimber")
                    override fun onResponse(
                        call: Call<DirectionsResponse>,
                        response: Response<DirectionsResponse>
                    ) {
                        val body = response.body() ?: return

                        if (body.routes().count() == 0) {
                            Log.e("MapNavigationActivity", "No route found.")
                            return
                        }
                        route = body.routes().first()

                        route?.geometry()?.let { it1 ->
                            val waypoints = PolylineUtils.decode(it1, 5)
                            geometry = LinkedList(waypoints)
                            nextPoint = null
                        }
                        /*
                        If in a travel session then, define the
                         */
                        val in_travel_session = ctx.findViewById<FloatingActionButton>(R.id.stopNavigationButton)
                        val out_of_traval_session = ctx.findViewById<FloatingActionButton>(R.id.startNavigationButton)
                        if (in_travel_session != null){
                            if(in_travel_session.visibility != View.VISIBLE) {
                                // clear all the pin point
                                ctx.displayRoute()
                            }
                            // The start navigation button should always be defined
                            if (BuildConfig.DEBUG && out_of_traval_session == null) {
                                error("Route Manager: start navigation button is not defined!")
                            }
                            // make sure the start button is always on
                            out_of_traval_session.visibility = View.VISIBLE
                        }else{
                            error("Route Manager: stop navigation button is not defined!")
                        }
                    }


                    @SuppressLint("LogNotTimber")
                    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                        Log.e("MapNavigationActivity", "Error: ${t.message}")
                    }
                })
        }

//        //display the route
//        if (navigationMapRoute != null){
//            navigationMapRoute?.updateRouteVisibilityTo(false)
//            navigationMapRoute?.updateRouteArrowVisibilityTo(false)
//            navigationMapRoute?.addRoute(route)
//        }

    }
}