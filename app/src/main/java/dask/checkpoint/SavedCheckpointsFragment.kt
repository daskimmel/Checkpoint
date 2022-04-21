package dask.checkpoint

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.support.design.button.MaterialButton
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_edit_checkpoint.*
import kotlinx.android.synthetic.main.fragment_saved_checkpoints.*
import kotlinx.android.synthetic.main.fragment_saved_checkpoints.view.*
import java.io.File


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SavedCheckpointsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SavedCheckpointsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class SavedCheckpointsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val file = getFile()
        val view = inflater.inflate(R.layout.fragment_saved_checkpoints, container, false)
        view.saved_checkpoints_ll.removeAllViews()
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(10, 5, 10, 5)

        val editText = TextView(this.context)
        editText.text = "CLICK TO EDIT"
        editText.gravity = Gravity.CENTER
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0F)
        editText.setTextColor(resources.getColorStateList(R.color.accent))
        view.saved_checkpoints_ll.addView(editText, lp)

        file.forEachLine{
            val checkpoint_values = it.split("**^**")
            val label = checkpoint_values[0]
            val color = checkpoint_values[1]
            val latitude = checkpoint_values[2]
            val longitude = checkpoint_values[3]
            val altitude = checkpoint_values[4]
            val speed = checkpoint_values[5]

            val savedCheckpointButton = MaterialButton(this.context)
            savedCheckpointButton.backgroundTintList = resources.getColorStateList(R.color.transparent)
            var buttonText = """${label} ${System.getProperty("line.separator")} ${System.getProperty("line.separator")}  Latitude: ${latitude} """
            buttonText += """${System.getProperty("line.separator")} Longitude: ${longitude} """
            buttonText += System.getProperty("line.separator")
            if((activity as MainActivity).isMetric){
                buttonText += """Altitude: ${altitude} m ${System.getProperty("line.separator")}"""
                buttonText += """Speed: ${"%.1f".format(speed.toDouble() * 3.6).toDouble()} km/h"""
            }else{
                buttonText += """Altitude: ${"%.1f".format(altitude.toDouble() * 3.28084).toDouble()} ft ${System.getProperty("line.separator")}"""
                buttonText += """Speed: ${"%.1f".format(speed.toDouble() * 2.23694).toDouble()} mph"""

            }
            savedCheckpointButton.text = buttonText
            savedCheckpointButton.setTextColor(
                ColorStateList.valueOf(Color.parseColor(color))
            )
            savedCheckpointButton.strokeColor = ColorStateList.valueOf(Color.parseColor(color))
            savedCheckpointButton.strokeWidth = 2
            savedCheckpointButton.setOnClickListener {
                val intent = Intent(this.context, EditCheckpoint::class.java)
                intent.putExtra("label", label)
                intent.putExtra("color", color)
                intent.putExtra("latitude", latitude)
                intent.putExtra("longitude", longitude)
                intent.putExtra("altitude", altitude)
                intent.putExtra("speed", speed)
                startActivityForResult(intent, 1)
            }
            view.saved_checkpoints_ll.addView(savedCheckpointButton, lp)
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
         * @return A new instance of fragment SavedCheckpointsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SavedCheckpointsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun getFile(): File {
        val filename = "saved_checkpoints_file"
        return File(this.context!!.filesDir, filename)
    }


}
