package com.example.hat

import android.annotation.SuppressLint
import android.util.Log
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

    fun getNextPoint(curr:Point): Point? {
        if(nextPoint==null){
            if(geometry==null){
                Log.e("route manager","empty geometry")
            }else{
                nextPoint = geometry!!.poll()
            }
        }else{
            val last = nextPoint?.latitude()?.div(10)?.let { Point.fromLngLat(
                nextPoint?.longitude()?.div(10)!!,it)
            }
            val distance = TurfMeasurement.distance(curr,last!!)
            Log.i("distance:",distance.toString())
            if(distance <0.1){
                if(!geometry?.isEmpty()!!){
                    nextPoint = geometry!!.poll()
                }
            }

        }
        val ret = nextPoint?.latitude()?.div(10)?.let { Point.fromLngLat(
            nextPoint?.longitude()?.div(10)!!,it)
        }

        return ret
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
                            geometry = LinkedList<Point>(waypoints)
                            nextPoint = null
                        }
                        ctx.displayRoute()


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