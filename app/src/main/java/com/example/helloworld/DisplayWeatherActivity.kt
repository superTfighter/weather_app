package com.example.helloworld

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.math.round

class DisplayWeatherActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    private var data : Weather? = null
    private val appid = "&appid=43a9f1572a829eefaf76bb03be055377"
    private var url = "https://api.openweathermap.org/data/2.5/weather?"

    //private var tempText : TextView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.displayweather)

        //tempText = this.temperature

        val intent = intent
        val lat = intent.getDoubleExtra("latitude", Double.NaN )
        val lon = intent.getDoubleExtra("longitude", Double.NaN )

        val uriAdd = "lat=$lat&lon=$lon"

        url += uriAdd
        url += appid


        run(url)
    }




    private fun run(url: String) {
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
                val  name : String = root.getString("name")


                val weather = JSONObject(JSONArray(root.getString("weather"))[0].toString())

                val main = JSONObject(root.getString("main"))
                val wind = JSONObject(root.getString("wind"))

                val ret = Weather(name, weather.getString("description") , main.getDouble("temp") - 273.15 , wind.getInt("speed") , main.getInt("humidity"))


                temperature.text = (round(ret.temp * 100)/100).toString()+" C°"
            }

        })

    }
}


data class Weather(val place : String , val description : String ,  val temp : Double , val windspeed : Int , val humidity : Int)

