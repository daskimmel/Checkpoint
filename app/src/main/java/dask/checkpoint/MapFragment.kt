package dask.checkpoint

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.custom_map_marker.view.*
import kotlinx.android.synthetic.main.fragment_create_new_checkpoint.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import java.io.File
import java.io.InputStream
import java.net.URL


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MapFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MapFragment : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: CreateNewCheckpointFragment.OnFragmentInteractionListener? = null

    var first_load = true
    var mapFragment : SupportMapFragment?=null
    private lateinit var map: GoogleMap
    private var locationUpdateState = false
    lateinit var lastLocation: Location

    lateinit var MAIN: MainActivity

    fun MapFragment() {
        // Required empty public constructor
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        MAIN = context as MainActivity
        if(first_load){
            if(MAIN.lastLocation != null){
                lastLocation = MAIN.lastLocation
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater!!.inflate(R.layout.fragment_map, container, false)
        mapFragment= childFragmentManager?.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment!!.getMapAsync(this)
        return view
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this.context!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this.context as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true
        val currentLatLng = LatLng(MAIN.lastLocation.latitude, MAIN.lastLocation.longitude)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))

        val file = getFile()
        file.forEachLine{
            val checkpoint_values = it.split("**^**")
            val label = checkpoint_values[0]
            val color = checkpoint_values[1]
            val latitude = checkpoint_values[2]
            val longitude = checkpoint_values[3]
            val altitude = checkpoint_values[4]
            val speed = checkpoint_values[5]

            val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
            placeMarkerOnMap(latLng, label, altitude, speed, color)
        }
    }

    private fun getFile(): File {
        val filename = "saved_checkpoints_file"
        val file = File(this.context!!.filesDir, filename)
        return file
    }

    private fun placeMarkerOnMap(location: LatLng, name: String, altitude: String, speed: String, color: String) {
        val markerOptions = MarkerOptions().position(location)

        lateinit var info: InfoWindowData

        if((activity as MainActivity).isMetric){
            info = InfoWindowData(name,
            """Latitude: ${location.latitude}""",
            """Longitude: ${location.longitude}""",
            """Altitude: ${altitude}""",
            """Speed: ${speed}""",
                color
            )
        }else{
            val new_alt = "%.1f".format((altitude.toDouble() * 3.28084))
            info = InfoWindowData(name,
                """Latitude: ${location.latitude}""",
                """Longitude: ${location.longitude}""",
                """Altitude: ${new_alt}""",
                """Speed: ${speed}""",
                color
            )

        }
//        markerOptions.snippet("""Latitude: ${location.latitude} Longitude ${location.longitude} \n
//            | Altitude: ${altitude} \n Speed: ${speed}""".trimMargin())

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(resources, R.mipmap.checkpoint_marker)))

        val marker = map.addMarker(markerOptions)
        marker.tag = info
    }


    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        val customInfoWindow = CustomInfoWindowGoogleMap(this.context!!)
        map.setInfoWindowAdapter(customInfoWindow)

        setUpMap()
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    data class InfoWindowData(val mLocatioName: String,
                              val mLocationLatitude: String,
                              val mLocationLongitude: String,
                              val mLocationAltitude: String,
                              val mLocationSpeed: String,
                              val mLocationColor: String)

    class CustomInfoWindowGoogleMap(val context: Context) : GoogleMap.InfoWindowAdapter {

        override fun getInfoContents(p0: Marker?): View {

            var mInfoView = (context as Activity).layoutInflater.inflate(R.layout.custom_map_marker, null)
            var mInfoWindow: InfoWindowData? = p0?.tag as InfoWindowData?
            mInfoView.setLayoutParams(LinearLayout.LayoutParams(500,300))

            mInfoView.txtLocMarkerName.text = mInfoWindow?.mLocatioName
            mInfoView.txtLocMarkerName.gravity = Gravity.CENTER
            mInfoView.txtLocMarkerName.setTypeface(null, Typeface.BOLD)
            mInfoView.txtLocMarkerAddress.text = mInfoWindow?.mLocationLatitude
            mInfoView.txtLocMarkerEmail.text = mInfoWindow?.mLocationLongitude
            mInfoView.txtLocMarkerPhone.text = mInfoWindow?.mLocationAltitude
            mInfoView.txtOpenningHoursValue.text = mInfoWindow?.mLocationSpeed
            mInfoView.checkpoint_color.setBackgroundColor(Color.parseColor(mInfoWindow?.mLocationColor))

            return mInfoView
        }

        override fun getInfoWindow(p0: Marker?): View? {
            return null
        }
    }
}
