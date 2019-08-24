package com.example.helloworld

import android.Manifest
import android.app.AlertDialog
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



class MainActivity : AppCompatActivity() {

    var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            fusedLocationClient?.lastLocation?.
                addOnSuccessListener(this
                ) { location : Location? ->
                    // Got last known location. In some rare
                    // situations this can be null.
                    if(location == null) {
                        // TODO, handle it
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
                /*val dialog = AlertDialog.Builder(this)
                    .setTitle("Permission")
                    .setMessage("Permission needed!")
                    .setPositiveButton("OK") { id, v ->
                        ActivityCompat.requestPermissions(
                            this, perm, PERMISSION_ID)
                    }
                    .setNegativeButton("No", {id, v -> })
                    .create()
                dialog.show()

                 */
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

                println("Permission not granted!")

            }
        }
    }
}








