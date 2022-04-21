package dask.checkpoint

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.WebViewFragment
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.io.File


class MainActivity : AppCompatActivity() {

    var currentView = "CREATE"

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    val REQUEST_CHECK_SETTINGS = 0x1

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var lastLocation: Location
    lateinit var locationCallback: LocationCallback
    lateinit var locationRequest: LocationRequest
    lateinit var fragmentManager: FragmentManager
    var isMetric = false

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                if(currentView!="CREATE"){
                    message.setText(R.string.title_home)
                    currentView = "CREATE"
                    switchFragment(CreateNewCheckpointFragment())
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                    return@OnNavigationItemSelectedListener true
                }
            }
            R.id.navigation_saved -> {
                if(currentView!="SAVED") {
                    message.setText(R.string.title_saved)
                    currentView = "SAVED"
                    switchFragment(SavedCheckpointsFragment())
                    return@OnNavigationItemSelectedListener true
                }
            }
            R.id.navigation_map -> {
                if(currentView!="MAP") {
                    currentView = "MAP"
                    message.setText(R.string.title_map)
                    switchFragment(MapFragment())
                    return@OnNavigationItemSelectedListener true
                }
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPermissions()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setIcon(R.drawable.icon_milestone_accent)

        message.setText(R.string.title_home)
        switchFragment(CreateNewCheckpointFragment())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createLocationRequest()

//        clearFile()
//        addCheckpoint("Red Rocks", "39.666508", "-105.207451", "1961.38", "0.0")
//        addCheckpoint("Kittredge", "39.6547", "-105.2997", "2104.94", "0.0")
//        addCheckpoint("Gym", "39.6542489", "-104.9983962", "1600.30", "0.0")
//        addCheckpoint("Home", "39.6489997", "-105.029682", "1590.30", "0.0")
//        addCheckpoint("Einstein Bros", "39.6536046", "-104.9954148", "1603.09", "0.0")


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                if(currentView=="CREATE") {
                    Log.e("ALTITUDE", lastLocation.altitude.toString())
                    Log.e("HAS ALTITUDE", lastLocation.hasAltitude().toString())
                    setLocation()
                }
            }
        }
    }

    fun clearFile(){
        val file = getFile()
        file.writeText("")
    }

    private fun getFile(): File {
        val filename = "saved_checkpoints_file"
        val file =  File(this.filesDir, filename)
        return File(this.filesDir, filename)
    }

    fun setLocation(){
        if(lastLocation != null){
            val altitude_val = findViewById<TextView>(R.id.altitude)
            val speed_val = findViewById<TextView>(R.id.speed)
            val latitude_val = findViewById<TextView>(R.id.latitude)
            val longitude_val = findViewById<TextView>(R.id.longitude)
            if(altitude_val != null && speed_val != null){
                if(isMetric){
                    altitude_val.text = """${"%.1f".format(lastLocation.altitude).toDouble()} m"""
                    speed_val.text = """${"%.1f".format(lastLocation.speed * 3.6).toDouble()} km/h"""
                }
                else{
                    altitude_val.text = """${"%.1f".format(lastLocation.altitude * 3.28084).toDouble()} ft"""
                    speed_val.text = """${"%.1f".format(lastLocation.speed * 2.23694).toDouble()} mph"""
                }
            }
            if(latitude_val != null && longitude_val != null) {
                latitude_val.text = lastLocation.latitude.toString()
                longitude_val.text = lastLocation.longitude.toString()
            }
        }
    }

    // called by fragment (passed as the parameter) to switch to the other fragment
    fun switchFragment(frag: Fragment) {
        when (frag) {
            is SavedCheckpointsFragment -> {
                // Insert the fragment by replacing any existing fragment
                fragmentManager = supportFragmentManager
                fragmentManager.beginTransaction().replace(R.id.main_fragment, frag).commitAllowingStateLoss()

            }
            is CreateNewCheckpointFragment -> {
                // Insert the fragment by replacing any existing fragment
                fragmentManager = supportFragmentManager
                fragmentManager.beginTransaction().replace(R.id.main_fragment, frag).commit()
            }
            is MapFragment -> {
                while(!::lastLocation.isInitialized){
                    Thread.sleep(100)
                }
                // Insert the fragment by replacing any existing fragment
                fragmentManager = supportFragmentManager
                fragmentManager.beginTransaction().replace(R.id.main_fragment, frag).commit()
            }
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    private fun setupPermissions() {

        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions,0)

        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("PERMISSION", "Permission to record denied")
        }
    }


    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            startLocationUpdates()
        }
        task.addOnFailureListener{exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (this.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
            } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }



    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val v = currentFocus

        if (v != null &&
            (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE) &&
            v is EditText &&
            !v.javaClass.name.startsWith("android.webkit.")
        ) {
            val scrcoords = IntArray(2)
            v.getLocationOnScreen(scrcoords)
            val x = ev.rawX + v.left - scrcoords[0]
            val y = ev.rawY + v.top - scrcoords[1]

            if (x < v.left || x > v.right || y < v.top || y > v.bottom)
                hideKeyboard(this)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun hideKeyboard(activity: Activity?) {
        if (activity != null && activity.window != null && activity.window.decorView != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CHECK_SETTINGS){
            setupPermissions()
            createLocationRequest()
        }else{
            if(currentView == "CREATE" && resultCode != Activity.RESULT_CANCELED){
                var fragment = supportFragmentManager.findFragmentById(R.id.main_fragment) as CreateNewCheckpointFragment
                fragment.colorPickerResult(data!!.extras.getString("color"))
            }else if(currentView=="SAVED"){
                switchFragment(SavedCheckpointsFragment())
            }
        }
    }

    fun addCheckpoint(label:String, latitude: String, longitude: String, altitude: String, speed: String){
        val file = getFile()
        val CheckpointString = """$label**^**#F67280**^**$latitude**^**$longitude**^**$altitude**^**$speed"""
        file.appendText(CheckpointString + System.getProperty("line.separator"))
    }

}
