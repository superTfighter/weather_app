package com.example.helloworld

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    private var data : Weather? = null

    private val url =  "https://api.openweathermap.org/data/2.5/weather?q=Budapest&appid=43a9f1572a829eefaf76bb03be055377"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        bigButton.setOnClickListener {
            run(url)

            if(data != null){

                val weather = data as Weather

                place.text = weather.place
                temp.text = weather.temp.toString() + " C°"
                humidity.text = weather.humidity.toString()+" %"
                desc.text = weather.description
            }


        }
    }




    private fun run(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                println("Failed to call api " + e.message)
            }
            override fun onResponse(call: Call, response: Response) {
                val root = JSONObject(response.body()?.string())

                response.body()?.close()
                val  name : String = root.getString("name")


                val weather = JSONObject(JSONArray(root.getString("weather"))[0].toString())

                val main = JSONObject(root.getString("main"))
                val wind = JSONObject(root.getString("wind"))

                val ret = Weather(name, weather.getString("description") , main.getDouble("temp") - 273.15 , wind.getInt("speed") , main.getInt("humidity"))


                data = ret

            }

        })

    }
}


data class Weather(val place : String , val description : String ,  val temp : Double , val windspeed : Int , val humidity : Int)






