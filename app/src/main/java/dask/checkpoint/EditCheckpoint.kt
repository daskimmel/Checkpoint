package dask.checkpoint

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_edit_checkpoint.*
import kotlinx.android.synthetic.main.custom_map_marker.view.*
import java.io.File

class EditCheckpoint : AppCompatActivity() {

    private lateinit var customColor: String
    var isMetric = false
    var speed_value = 0.0
    var altitude_value = 0.0
    var checkpoint_label = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_checkpoint)

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setIcon(R.drawable.icon_milestone_accent)
        val intent_extras = intent.extras

        checkpoint_label = intent_extras.getString("label")
        checkpoint_label_edit.setText(checkpoint_label)
        customColor = intent_extras.getString("color")
        latitude.text = intent_extras.getString("latitude")
        longitude.text = intent_extras.getString("longitude")
        speed_value = intent_extras.getString("speed").toDouble()
        altitude_value = intent_extras.getString("altitude").toDouble()
        setLocation()

        if(customColor != "#F67280"){
            choose_color.backgroundTintList = ColorStateList.valueOf(Color.parseColor(customColor))

            var red = Integer.parseInt(customColor.substring(1, 3), 16)
            var green = Integer.parseInt(customColor.substring(3, 5), 16)
            var blue = Integer.parseInt(customColor.substring(5, 7), 16)

            var luma = 0.2126 * red + 0.7152 * green + 0.0722 * blue

            if (luma < 120) {
                choose_color.setTextColor(Color.WHITE)
            }else{
                choose_color.setTextColor(Color.BLACK)
            }
        }

        units_switch.setOnCheckedChangeListener { view, isChecked ->
            if(isChecked){
                isMetric = true
                units_text.text = "METRIC"
            }else{
                isMetric = false
                units_text.text = "IMPERIAL"
            }
            setLocation()
        }

        delete_checkpoint.setOnClickListener{
            showDialog()
        }
    }

    fun setLocation(){
        val altitude_val = findViewById<TextView>(R.id.altitude)
        val speed_val = findViewById<TextView>(R.id.speed)
        if(altitude_val != null && speed_val != null) {
            if (isMetric) {
                altitude_val.text = """${"%.1f".format(altitude_value).toDouble()} m"""
                speed_val.text = """${"%.1f".format(speed_value * 3.6).toDouble()} km/h"""
            } else {
                altitude_val.text = """${"%.1f".format(altitude_value * 3.28084).toDouble()} ft"""
                speed_val.text = """${"%.1f".format(speed_value * 2.23694).toDouble()} mph"""
            }
        }
    }

    fun launchColorPicker(view: View){
        val launchIntent = Intent("com.example.colorpicker.ACTION_COLOR")
        launchIntent.putExtra("COLOR", "")
        if (launchIntent != null) {
            Log.e("launchIntent:", "SUCCEEDED")
            startActivityForResult(launchIntent, 1)
        }else{
            Log.e("launchIntent:", "FAILED")

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Check which request we're responding to
        Log.e("4 ", "4 ")
        val extras = data?.extras
        if (resultCode == Activity.RESULT_OK) {
            if(extras != null){
                customColor = extras?.getString("color")
                choose_color.backgroundTintList = ColorStateList.valueOf(Color.parseColor(customColor))

                var red = Integer.parseInt(customColor.substring(1, 3), 16)
                var green = Integer.parseInt(customColor.substring(3, 5), 16)
                var blue = Integer.parseInt(customColor.substring(5, 7), 16)

                var luma = 0.2126 * red + 0.7152 * green + 0.0722 * blue

                if (luma < 120) {
                    choose_color.setTextColor(Color.WHITE)
                }else{
                    choose_color.setTextColor(Color.BLACK)
                }
            }
        }
    }

    fun saveEditedCheckpoint(view: View){
        if(isNullOrEmpty(checkpoint_label)){
            Toast.makeText(this, "Please enter a label", Toast.LENGTH_LONG).show()
        }else{
            val file = getFile()
            val fileList = ArrayList<String>()
            file.forEachLine {
                fileList.add(it)
            }
            file.writeText("")

            for(checkpoint in fileList){
                val checkpoint_values = checkpoint.split("**^**")
                val label = checkpoint_values[0]
                val color = checkpoint_values[1]
                val latitude = checkpoint_values[2]
                val longitude = checkpoint_values[3]
                val altitude = checkpoint_values[4]
                val speed = checkpoint_values[5]
                var CheckpointString = ""

                if(label == checkpoint_label){
                    val new_label = checkpoint_label_edit.text.toString()
                    CheckpointString = """$new_label**^**$customColor**^**$latitude**^**$longitude**^**$altitude**^**$speed"""
                }else{
                    CheckpointString = """$label**^**$color**^**$latitude**^**$longitude**^**$altitude**^**$speed"""
                }
                file.appendText(CheckpointString + System.getProperty("line.separator"))
            }

            Toast.makeText(this, "Checkpoint Saved", Toast.LENGTH_LONG).show()
            val i = Intent()
            i.putExtra("save", "okay")
            setResult(RESULT_OK, i)
            finish()
        }
    }

    fun isNullOrEmpty(str: String?): Boolean {
        if (str != null && !str.trim().isEmpty())
            return false
        return true
    }

    fun deleteEditedCheckpoint(){
        val file = getFile()
        val fileList = ArrayList<String>()
        file.forEachLine {
            fileList.add(it)
        }
        file.writeText("")

        for(checkpoint in fileList){
            val checkpoint_values = checkpoint.split("**^**")
            val label = checkpoint_values[0]
            val color = checkpoint_values[1]
            val latitude = checkpoint_values[2]
            val longitude = checkpoint_values[3]
            val altitude = checkpoint_values[4]
            val speed = checkpoint_values[5]
            var CheckpointString = ""

            if(label != checkpoint_label){
                CheckpointString = """$label**^**$color**^**$latitude**^**$longitude**^**$altitude**^**$speed"""
                file.appendText(CheckpointString + System.getProperty("line.separator"))
            }
        }

        Toast.makeText(this, "Checkpoint Deleted", Toast.LENGTH_LONG).show()

        val i = Intent()
        i.putExtra("delete", "okay")
        setResult(RESULT_OK, i)
        finish()
    }

    private fun getFile(): File {
        val filename = "saved_checkpoints_file"
        return File(this.filesDir, filename)
    }

    // Method to show an alert dialog with yes, no and cancel button
    private fun showDialog(){
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Are you sure?")
        builder.setMessage("Choosing yes will delete this CheckPoint.")
        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> deleteEditedCheckpoint()
                DialogInterface.BUTTON_NEGATIVE -> null
            }
        }
        builder.setPositiveButton("YES",dialogClickListener)
        builder.setNegativeButton("NO",dialogClickListener)
        dialog = builder.create()
        dialog.show()

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(R.color.transparent)
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000000"))
    }
}

