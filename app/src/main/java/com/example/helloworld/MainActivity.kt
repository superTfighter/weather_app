package com.example.helloworld

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.content_main.*
import android.content.Intent
import android.content.SharedPreferences
import java.io.*


class MainActivity : AppCompatActivity() {

    var fusedLocationClient: FusedLocationProviderClient? = null

    private val PREFS_NAME = "kotlincodes"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPref: SharedPreferences = this@MainActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        submit_button.setOnClickListener {

            val location = location_text.text

            if(!location.isNullOrEmpty()){

                var intent = Intent(this@MainActivity, DisplayWeatherActivity::class.java)
                intent.putExtra("location", location.toString())

                val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.putString("location", location.toString())
                editor.apply()

                startActivity(intent)
            }else{
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("ERROR")
                builder.setMessage("Nem írtál be helyszínt!\nKérlek írj be egy megfelelő helyszínt!")
                builder.setPositiveButton("Ok"){dialog, which ->
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }


        }

        if (checkPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            fusedLocationClient?.lastLocation?.
                addOnSuccessListener(this
                ) { location : Location? ->
                    // Got last known location. In some rare
                    // situations this can be null.
                    if(location == null) {


                        val prefLoc  = sharedPref.getString("location", null)

                        if(prefLoc != null) {

                            var intent = Intent(this@MainActivity, DisplayWeatherActivity::class.java)
                            intent.putExtra("location", prefLoc.toString())

                            startActivity(intent)
                        }
                    } else location.apply {

                        var intent = Intent(this@MainActivity,DisplayWeatherActivity::class.java)
                        intent.putExtra("latitude" , location.latitude)
                        intent.putExtra("longitude",location.longitude)

                        startActivity(intent)
                    }
                }
        }

    }


    private val PERMISSION_ID = 42
    private fun checkPermission(vararg perm:String) : Boolean {
        val havePermissions = perm.toList().all {
            ContextCompat.checkSelfPermission(this,it) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (!havePermissions) {
            if(perm.toList().any {
                    ActivityCompat.
                        shouldShowRequestPermissionRationale(this, it)}
            ) {

                ActivityCompat.requestPermissions(this, perm, PERMISSION_ID)
            } else {
                ActivityCompat.requestPermissions(this, perm, PERMISSION_ID)
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_ID -> {

                val sharedPref: SharedPreferences = this@MainActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val prefLoc  = sharedPref.getString("location", null)

                if(prefLoc != null) {

                    var intent = Intent(this@MainActivity, DisplayWeatherActivity::class.java)
                    intent.putExtra("location", prefLoc.toString())

                    startActivity(intent)
                }

            }
        }
    }
}










