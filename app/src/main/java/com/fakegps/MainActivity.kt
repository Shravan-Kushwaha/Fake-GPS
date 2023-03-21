package com.fakegps

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fakegps.databinding.ActivityMainBinding
import com.fakegps.extension.showErrorAlert
import com.fakegps.extension.showMsgDialog
import com.fakegps.utils.ConstantUtil.GOOGLE_MAP_ZOOM_LEVEL
import com.fakegps.utils.Logger
import com.fakegps.utils.PermissionClass
import com.fakegps.utils.PermissionManagerUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveCanceledListener {
    private var REQUEST_LOCATION_CODE = 101
    private lateinit var mMap: GoogleMap
    var mFusedLocationClient: FusedLocationProviderClient? = null
    private var location = LatLng(0.0, 0.0)
    private lateinit var binding: ActivityMainBinding
    private var mockLocation: MockLocationImpl? = null
    private var mNotificationIntent: Intent? = null
    private var isRunning = false
    var longitudeText = 0.0
    var latitudeText = 0.0
    private var latitude = 0.0
    var longitude = 0.0
    private lateinit var analytics: FirebaseAnalytics
    private var mPreferences: SharedPreferences? = null
    private var mEditor: SharedPreferences.Editor? = null

    private val POWERMANAGER_INTENTS = arrayOf(
        Intent().setComponent(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.startupapp.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.ui.battery.BatteryActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.htc.pitroad",
                "com.htc.pitroad.landingpage.activity.LandingPageActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.MainActivity"
            )
        )
    )

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        analytics = Firebase.analytics
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationPermission()
        } else {
            checkLocationPermission()
        }

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        binding.adView.adListener = object : AdListener() {
            override fun onAdClicked() {
              //  Toast.makeText(this@MainActivity, "onAdClicked", Toast.LENGTH_SHORT).show()
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
             //   Toast.makeText(this@MainActivity, "onAdClosed", Toast.LENGTH_SHORT).show()
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
              //  Toast.makeText(this@MainActivity, "onAdFailedToLoad", Toast.LENGTH_SHORT).show()
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
               // Toast.makeText(this@MainActivity, "onAdImpression", Toast.LENGTH_SHORT).show()
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                //Toast.makeText(this@MainActivity, "onAdLoaded", Toast.LENGTH_SHORT).show()
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdOpened() {
               // Toast.makeText(this@MainActivity, "onAdOpened", Toast.LENGTH_SHORT).show()
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }

        mPreferences = this.getSharedPreferences("FAKE_GPS", MODE_PRIVATE)
        mEditor = mPreferences!!.edit()

        val mNeedPref: Boolean = mPreferences!!.getBoolean("NEED_PREF", true)
        for (intent in POWERMANAGER_INTENTS) if (this.packageManager
                .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null && mNeedPref
        ) {
            startActivity(intent)
            break
        }

        mockLocation = MockLocationImpl(this)

        mNotificationIntent =
            Intent(applicationContext, NotificationService::class.java)
        init()
        binding.floatingActionButton.setOnClickListener {
            if (!isMockLocationEnabled()) {
                Toast.makeText(
                    this,
                    "Please turn on Mock Location permission on Developer Settings",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                return@setOnClickListener
            }
            if (isRunning) {
                isRunning = false
                binding.floatingActionButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@MainActivity,
                        android.R.drawable.ic_media_play
                    )
                )
                this.stopService(mNotificationIntent)
                mockLocation!!.stopMockLocationUpdates()
            } else {
                isRunning = true
                binding.floatingActionButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@MainActivity,
                        android.R.drawable.ic_media_pause
                    )
                )
                mockLocation!!.startMockLocationUpdates(
                    location.latitude,
                    location.longitude
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(mNotificationIntent)
                } else {
                    this.startService(mNotificationIntent)
                }
            }

        }
        binding.etPick.setOnClickListener {
            startAutocompleteIntent()
        }
    }

    private fun startAutocompleteIntent() {

        // Set the fields to specify which types of place data to
        // return after the user has made a selection.

        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        // Build the autocomplete intent with field, country, and type filters applied
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            /* .setCountry("US")*/
            /*.setCountry("IN")*/
            .build(this)
        startAutocomplete.launch(intent)
    }

    private val startAutocomplete = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    try {
                        val place = Autocomplete.getPlaceFromIntent(intent)
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    place.latLng?.latitude!!,
                                    place.latLng?.longitude!!
                                ), GOOGLE_MAP_ZOOM_LEVEL
                            )
                        )
                        binding.etSource.text = place.address
                        binding.etPick.text = place.address
                        //userPreferences?.address = place.address
                        //returnIntent.putExtra("pickupLat", place.latLng?.latitude.toString())
                       // returnIntent.putExtra("pickupLong", place.latLng?.longitude.toString())
                    } catch (e: Exception) {
                        showErrorAlert(getString(R.string.general_something_went_wrong))
                    }
                }
            } else if (result.resultCode == RESULT_CANCELED) {
                Log.i("TAG", "User canceled autocomplete")
            }
        } as ActivityResultCallback<ActivityResult>)

    private fun checkLocationPermission(): Boolean {
        var isPermission = false
        if (isLocationEnabled()) {
            val requiredPermissionList =
                arrayListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

            PermissionClass.checkPermission(
                this,
                this,
                requiredPermissionList,
                PermissionClass.PermissionSessionManager(this),
                object : PermissionClass.PermissionAskListener {
                    override fun onNeedPermission() {
                        isPermission = false
                        Logger.d("onNeedPermission")
                        ActivityCompat.requestPermissions(
                            (this@MainActivity), arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ), REQUEST_LOCATION_CODE
                        )
                    }

                    override fun onPermissionPreviouslyDenied() {
                        isPermission = false
                        Logger.d("onPermissionPreviouslyDenied")
                        ActivityCompat.requestPermissions(
                            (this@MainActivity), arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ), REQUEST_LOCATION_CODE
                        )
                    }

                    override fun onPermissionPreviouslyDeniedWithNeverAskAgain() {
                        isPermission = false
                        Logger.d(
                            "onPermissionPreviouslyDeniedWithNeverAskAgain"
                        )
                        ActivityCompat.requestPermissions(
                            (this@MainActivity), arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ), REQUEST_LOCATION_CODE
                        )
                        /*(this@MapsActivity).showMsgDialog(
                            (this@MapsActivity).resources.getString(R.string.location_permissions_needed),
                            (this@MapsActivity).resources.getString(R.string.open_setting),
                            { _, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

                                val uri: Uri =
                                    Uri.fromParts("package", (this@MapsActivity).packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }, "cancel",
                            { _, _ ->

                            }
                        )*/
                    }

                    override fun onPermissionGranted() {
                        isPermission = true
                        getCurrentLocation()
                    }

                })
        } else {
            this.showMsgDialog(
                this.resources.getString(R.string.location_permissions_needed),
                this.resources.getString(R.string.open_setting),
                { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }, "cancel",
                { _, _ ->

                }
            )

        }
        return isPermission
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun init() {
        val locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val networkLoc = Objects.requireNonNull(locationManager)
            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (networkLoc != null && !isRunning) {
            longitudeText = networkLoc.longitude
            latitudeText = networkLoc.latitude
        } else if (!isRunning) {
            latitudeText = mPreferences!!.getFloat("LATITUDE", 1.0f).toDouble()
            longitudeText = mPreferences!!.getFloat("LONGITUDE", 1.0f).toDouble()
        }
        latitude = latitudeText
        longitude = longitudeText

        if (longitude != 0.0) {
            longitudeText = longitude
            if (longitudeText <= 180.0 && longitudeText >= -180.0) {
                mEditor!!.putFloat("LONGITUDE", longitudeText.toFloat())
                mEditor!!.apply()
            }
        }


        if (latitude != 0.0) {
            latitudeText = latitude
            if (latitudeText <= 90.0 && latitudeText >= -90.0) {
                mEditor!!.putFloat("LATITUDE", latitudeText.toFloat())
                mEditor!!.apply()
            }
        }
    }

    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationClient?.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()!!
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation

            if (mLastLocation != null) {
                Log.e(
                    "MAP",
                    "From Result - Lat : " + mLastLocation.latitude + "  lng : " + mLastLocation.longitude
                )

            }
        }
    }

    private fun getAddressText(location: GeoPoint): String? {
        var addresses: List<Address>? = null
        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 2)
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            assert(addresses != null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addresses!![0].getAddressLine(0)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {

        mFusedLocationClient!!.lastLocation.addOnCompleteListener { task ->
            try {
                val location = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    this.location = LatLng(location.latitude, location.longitude)
                    /*  mMap.addMarker(
                          MarkerOptions().position(this.location).title(
                              getAddressText(
                                  GeoPoint(
                                      this.location.latitude,
                                      this.location.longitude
                                  )
                              )
                          )
                      )*/
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                this.location.latitude, this.location.longitude
                            ),
                            GOOGLE_MAP_ZOOM_LEVEL
                        )
                    )
                }
            } catch (e: RuntimeExecutionException) {
                e.localizedMessage?.let { showErrorAlert(it) }
            } catch (e: Exception) {
                e.localizedMessage?.let { showErrorAlert(it) }
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_CODE) {
            checkLocationPermission()
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true


        mMap.setOnCameraIdleListener(this@MainActivity)
        mMap.setOnCameraMoveStartedListener(this@MainActivity)
        mMap.setOnCameraMoveListener(this@MainActivity)
        mMap.setOnCameraMoveCanceledListener(this@MainActivity)
        latitude = googleMap.cameraPosition.target.latitude
        longitude = googleMap.cameraPosition.target.longitude
        try {
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        latitude, longitude
                    ),
                    GOOGLE_MAP_ZOOM_LEVEL
                )
            )

        } catch (e: Exception) {
            Log.e("TAG", "onMapReady: ${e.printStackTrace()}")
        }
    }

    private fun checkLocationPermissions(): Boolean {
        var isPermission = false
        if (isLocationEnabled()) {
            val requiredPermissionList =
                arrayListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

            PermissionManagerUtils.checkPermission(
                this,
                this,
                requiredPermissionList,
                PermissionManagerUtils.PermissionSessionManager(this),
                object : PermissionManagerUtils.PermissionAskListener {
                    override fun onNeedPermission() {
                        isPermission = false

                        ActivityCompat.requestPermissions(
                            (this@MainActivity), arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ), REQUEST_LOCATION_CODE
                        )
                    }

                    override fun onPermissionPreviouslyDenied() {
                        isPermission = false
                        ActivityCompat.requestPermissions(
                            (this@MainActivity), arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ), REQUEST_LOCATION_CODE
                        )
                    }

                    override fun onPermissionPreviouslyDeniedWithNeverAskAgain() {
                        isPermission = false
                        (this@MainActivity).showMsgDialog(
                            (this@MainActivity).resources.getString(R.string.location_permissions_needed),
                            (this@MainActivity).resources.getString(R.string.open_setting),
                            { _, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

                                val uri: Uri =
                                    Uri.fromParts("package", (this@MainActivity).packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }, "cancel",
                            { _, _ ->

                            }
                        )
                    }

                    override fun onPermissionGranted() {
                        isPermission = true
                        getCurrentLocation()
                    }

                })
        } else {
            this.showMsgDialog(
                this.resources.getString(R.string.location_permissions_needed),
                this.resources.getString(R.string.open_setting),
                { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }, "cancel",
                { _, _ ->

                }
            )

        }
        return isPermission
    }

    private fun isMockLocationEnabled(): Boolean {
        val isMockLocation: Boolean
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val opsManager = this.getSystemService(APP_OPS_SERVICE) as AppOpsManager
                isMockLocation = Objects.requireNonNull(opsManager).checkOp(
                    AppOpsManager.OPSTR_MOCK_LOCATION,
                    Process.myUid(),
                    BuildConfig.APPLICATION_ID
                ) == AppOpsManager.MODE_ALLOWED
            } else {
                isMockLocation =
                    Settings.Secure.getString(this.contentResolver, "mock_location") != "0"
            }
        } catch (e: java.lang.Exception) {
            return false
        }
        return isMockLocation
    }


    fun finish(view: View) {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRunning) {
            mockLocation!!.stopMockLocationUpdates()
            isRunning = false
        }
    }

    override fun onCameraMoveStarted(reason: Int) {
        when (reason) {
            GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                /*Toast.makeText(
                    this, "The user gestured on the map.",
                    Toast.LENGTH_SHORT
                ).show()*/
            }
            GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION -> {
                /*Toast.makeText(
                    this, "The user tapped something on the map.",
                    Toast.LENGTH_SHORT
                ).show()*/
            }
            GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> {
                /*Toast.makeText(
                    this, "The app moved the camera.",
                    Toast.LENGTH_SHORT
                ).show()*/
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCameraIdle() {
        try {
            location = mMap.cameraPosition.target
            /*mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    location,
                    GOOGLE_MAP_ZOOM_LEVEL
                )
            )*/

            if (location.latitude != 0.0 && location.longitude != 0.0) {
                binding.etSource.text = getAddressText(
                    GeoPoint(
                        location.latitude,
                        location.longitude
                    )
                )
                if (isRunning) {
                    mockLocation!!.startMockLocationUpdates(
                        location.latitude,
                        location.longitude
                    )
                }

            }

        } catch (e: Exception) {
            Log.e("TAG", "setMarker: ${e.message}")
        }
    }


    override fun onCameraMoveCanceled() {
        /*Toast.makeText(
            this, "Camera movement canceled.",
            Toast.LENGTH_SHORT
        ).show()*/
    }

    override fun onCameraMove() {
        /* Toast.makeText(
             this, "The camera is moving.",
             Toast.LENGTH_SHORT
         ).show()*/
    }
}