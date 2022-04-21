package dask.checkpoint

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_create_new_checkpoint.*
import kotlinx.android.synthetic.main.fragment_create_new_checkpoint.view.*
import java.io.File
import java.lang.Integer.parseInt


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CreateNewCheckpointFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CreateNewCheckpointFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class CreateNewCheckpointFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var customColor: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        customColor = "#F67280"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view: View = inflater!!.inflate(R.layout.fragment_create_new_checkpoint, container, false)

        if((activity as MainActivity).isMetric){
            view.units_switch.isChecked = true
            view.units_text.text = "METRIC"
        }

        view.units_switch.setOnCheckedChangeListener { view, isChecked ->
            if(isChecked){
                (activity as MainActivity).isMetric = true
                units_text.text = "METRIC"
            }else{
                (activity as MainActivity).isMetric = false
                units_text.text = "IMPERIAL"
            }
            (activity as MainActivity).setLocation()
        }

        view.choose_color.setOnClickListener { view ->
            launchColorPicker()
        }

        view.save_checkpoint.setOnClickListener { view ->
            saveCheckpoint()
        }

        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CreateNewCheckpointFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateNewCheckpointFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    fun launchColorPicker(){
        val launchIntent = Intent("com.example.colorpicker.ACTION_COLOR")
        launchIntent.putExtra("COLOR", "")
        if (launchIntent != null) {
            Log.e("launchIntent:", "SUCCEEDED")
            startActivityForResult(launchIntent, 1)
        }else{
            Log.e("launchIntent:", "FAILED")

        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        // Check which request we're responding to
//        val extras = data!!.extras
//        Log.e("REQUEST CODE", requestCode.toString())
//        if(requestCode == (activity as MainActivity).REQUEST_CHECK_SETTINGS){
//            if(resultCode == Activity.RESULT_OK){
//                Log.e("LOCATION ON", "LOCATION ON")
//            }else{
//                Log.e("LOCATION OFF", "LOCATION OFF")
//            }
//        }else{
//            if (resultCode == Activity.RESULT_OK) {
//                if(extras != null){
//                    customColor = extras.getString("color")
//                    choose_color.backgroundTintList = ColorStateList.valueOf(Color.parseColor(customColor))
//
//                    val red = parseInt(customColor.substring(1,3), 16)
//                    val green = parseInt(customColor.substring(3,5), 16)
//                    val blue = parseInt(customColor.substring(5,7), 16)
//
//                    val luma = 0.2126 * red + 0.7152 * green + 0.0722 * blue
//
//                    if (luma < 120) {
//                        choose_color.setTextColor(Color.WHITE)
//                    }else{
//                        choose_color.setTextColor(Color.BLACK)
//                    }
//                }
//            }
//
//            if((activity as MainActivity).currentView=="SAVED"){
//                (activity as MainActivity).switchFragment(SavedCheckpointsFragment())
//            }
//        }
//    }

    fun colorPickerResult(color: String){
        customColor = color
        choose_color.backgroundTintList = ColorStateList.valueOf(Color.parseColor(customColor))

        val red = parseInt(customColor.substring(1,3), 16)
        val green = parseInt(customColor.substring(3,5), 16)
        val blue = parseInt(customColor.substring(5,7), 16)

        val luma = 0.2126 * red + 0.7152 * green + 0.0722 * blue

        if (luma < 120) {
            choose_color.setTextColor(Color.WHITE)
        }else{
            choose_color.setTextColor(Color.BLACK)
        }

        if((activity as MainActivity).currentView=="SAVED"){
            (activity as MainActivity).switchFragment(SavedCheckpointsFragment())
        }
    }

    fun saveCheckpoint(){
        val file = getFile()
        val label = checkpoint_label.text.toString()
        val latitude = (activity as MainActivity).lastLocation.latitude.toString()
        val longitude = (activity as MainActivity).lastLocation.longitude.toString()
        val altitude = (activity as MainActivity).lastLocation.altitude.toString()
        val speed = (activity as MainActivity).lastLocation.speed.toString()
        val CheckpointString = """$label**^**$customColor**^**$latitude**^**$longitude**^**$altitude**^**$speed"""
        if(isNullOrEmpty(label)){
            Toast.makeText(this.context, "Please enter a label", Toast.LENGTH_LONG).show()
        }else if(checkForLabel(label)){
            Toast.makeText(this.context, "CheckPoint label already exists", Toast.LENGTH_LONG).show()
        }else{
            file.appendText(CheckpointString + System.getProperty("line.separator"))
            Toast.makeText(this.context, "Checkpoint Saved", Toast.LENGTH_LONG).show()
            checkpoint_label.text.clear()
            choose_color.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.transparent))
            choose_color.setTextColor(resources.getColor(R.color.accent_two))
        }
    }

    fun checkForLabel(label: String): Boolean {
        var check = false
        val file = getFile()
        if(file.exists()){
            file.forEachLine {
                val checkpoint_values = it.split("**^**")
                val existing_label = checkpoint_values[0]
                if(existing_label == label){
                    check = true
                }
            }
        }
        return check
    }

    fun isNullOrEmpty(str: String?): Boolean {
        if (str != null && !str.trim().isEmpty())
            return false
        return true
    }

    private fun getFile(): File {
        val filename = "saved_checkpoints_file"
        return File(this.context!!.filesDir, filename)
    }

}
