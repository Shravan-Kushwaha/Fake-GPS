package com.fakegps

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi


class MockLocationImpl internal constructor(context: Context) {
    private val mLocationManager: LocationManager
    private val mHandler: Handler
    private var mRunnable: Runnable? = null

    init {
        mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mHandler = Handler()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun startMockLocationUpdates(latitude: Double, longitude: Double) {
        mRunnable = Runnable {
            setMock(LocationManager.GPS_PROVIDER, latitude, longitude)
            setMock(LocationManager.NETWORK_PROVIDER, latitude, longitude)
            mHandler.postDelayed(mRunnable!!, 500)
            Log.e("TAG", "startMockLocationUpdates: latitude - $latitude longitude - $longitude" )
        }
        mHandler.post(mRunnable!!)
    }

    fun stopMockLocationUpdates() {
        mHandler.removeCallbacks(mRunnable!!)
        try {
            if (mLocationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
                mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            }
            if (mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
                mLocationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
            }
        } catch (e: Exception) {
            Log.e("TAG", "Exception: ${e.localizedMessage} ${e.printStackTrace()}")
        }
    }

    private fun setMock(provider: String, latitude: Double, longitude: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val newLocation = Location(provider)
            newLocation.latitude = latitude
            newLocation.longitude = longitude
            newLocation.time = System.currentTimeMillis()
            newLocation.accuracy = Criteria.ACCURACY_FINE.toFloat()
            newLocation.altitude = 3.0
            newLocation.speed = 0.01f
            newLocation.bearing = 1f
            newLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                newLocation.bearingAccuracyDegrees = 0.1f
                newLocation.verticalAccuracyMeters = 0.1f
                newLocation.speedAccuracyMetersPerSecond = 0.01f
            }
            try {

                // @throws IllegalArgumentException if a provider with the given name already exists
                mLocationManager.addTestProvider(
                    provider,
                    false,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    ProviderProperties.POWER_USAGE_LOW,
                    ProviderProperties.ACCURACY_FINE
                )
            } catch (ignored: IllegalArgumentException) {
            }

            try {
                // @throws IllegalArgumentException if no provider with the given name exists
                mLocationManager.setTestProviderEnabled(provider, true)
            } catch (ignored: IllegalArgumentException) {
                mLocationManager.addTestProvider(
                    provider,
                    false,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    ProviderProperties.POWER_USAGE_LOW,
                    ProviderProperties.ACCURACY_FINE
                )
            }

            try {
                // @throws IllegalArgumentException if no provider with the given name exists
                mLocationManager.setTestProviderLocation(provider, newLocation)
            } catch (ignored: IllegalArgumentException) {
                mLocationManager.addTestProvider(
                    provider,
                    false,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    ProviderProperties.POWER_USAGE_LOW,
                    ProviderProperties.ACCURACY_FINE
                )
                mLocationManager.setTestProviderEnabled(provider, true)
                mLocationManager.setTestProviderLocation(provider, newLocation)
            }
        }else{
        val newLocation = Location(provider)
        newLocation.latitude = latitude
        newLocation.longitude = longitude
        newLocation.time = System.currentTimeMillis()
        newLocation.accuracy = Criteria.ACCURACY_FINE.toFloat()
        newLocation.altitude = 3.0
        newLocation.speed = 0.01f
        newLocation.bearing = 1f
        newLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            newLocation.bearingAccuracyDegrees = 0.1f
            newLocation.verticalAccuracyMeters = 0.1f
            newLocation.speedAccuracyMetersPerSecond = 0.01f
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mLocationManager.addTestProvider(
                provider,
                false,
                false,
                false,
                false,
                false,
                true,
                true,
                ProviderProperties.POWER_USAGE_LOW,
                ProviderProperties.ACCURACY_FINE
            )
        }
        try {
            mLocationManager.setTestProviderEnabled(provider, true)
            mLocationManager.setTestProviderLocation(provider, newLocation)
        } catch (e: Exception) {
            Log.e("TAG", "Exception: ${e.localizedMessage} ${e.printStackTrace()}")
        }
    }
    }
}