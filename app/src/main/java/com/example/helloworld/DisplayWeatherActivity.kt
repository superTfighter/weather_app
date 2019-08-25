package com.example.helloworld

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main._displayweather.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import kotlin.math.round


class DisplayWeatherActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val appid = "&lang=hu&appid=43a9f1572a829eefaf76bb03be055377"
    private var url = "https://api.openweathermap.org/data/2.5/weather?"
    private var forecastUrl= "https://api.openweathermap.org/data/2.5/forecast?"


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.displayweather)

        val intent = intent
        val lat = intent.getDoubleExtra("latitude", Double.NaN )
        val lon = intent.getDoubleExtra("longitude", Double.NaN )
        var uriAdd: String

        if(lat.isNaN()){
             val location = intent.getStringExtra("location")
            uriAdd = "q=$location"

        }else{
             uriAdd = "lat=$lat&lon=$lon"
        }

        url += uriAdd
        url += appid

        forecastUrl += uriAdd
        forecastUrl += appid


        run(url,forecastUrl)
    }




    private fun run(url: String , forecastUrl : String) {
        val request = Request.Builder()
            .url(url)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                println("Failed to call api " + e.message)
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                val root = JSONObject(response.body()?.string())
                response.body()?.close()

                if(root.has("name")){
                    val  name : String = root.getString("name")
                    val weather = JSONObject(JSONArray(root.getString("weather"))[0].toString())

                    val main = JSONObject(root.getString("main"))
                    val wind = JSONObject(root.getString("wind"))

                    val ret = Weather(
                        name,
                        weather.getString("description"),
                        main.getDouble("temp") - 273.15,
                        wind.getInt("speed"),
                        main.getInt("humidity")
                    )
                    val imageIcon = weather.getString("icon")

                    temperature.text = (round(ret.temp * 100) / 100).toString() + "°"
                    humidity.text = ret.humidity.toString() + " %"
                    location.text = ret.place.replace("keruelet", "kerület")
                    description.text = ret.description
                    wind_text.text = ret.windspeed.toString() + " km/h"

                    val bmp: Bitmap
                    val imageUrl = "http://openweathermap.org/img/wn/$imageIcon@2x.png"

                    val inp: InputStream = URL(imageUrl).openStream()
                    bmp = BitmapFactory.decodeStream(inp)

                    runOnUiThread {
                        iconImage.setImageBitmap(bmp)
                    }




                }else{

                    runOnUiThread {
                        val builder = AlertDialog.Builder(this@DisplayWeatherActivity)
                        builder.setTitle("ERROR!")
                        builder.setMessage("Nem megfelelő helyszín!\nKérlek megfelelő helyszínt adj meg!")
                        builder.setPositiveButton("Ok"){dialog, which ->
                        }
                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                    }
                }
            }
        })

    }


    private fun forecast(url : String){

        val request = Request.Builder()
            .url(url)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                println("Failed to call api " + e.message)
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                val root = JSONObject(response.body()?.string())
                response.body()?.close()

                println(root)
            }
        })



    }
}


data class Weather(val place : String , val description : String ,  val temp : Double , val windspeed : Int , val humidity : Int)



