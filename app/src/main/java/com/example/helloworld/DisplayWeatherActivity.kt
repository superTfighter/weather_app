package com.example.helloworld

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import kotlinx.android.synthetic.main._displayweather.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import kotlin.math.round
import java.time.ZoneId.systemDefault
import kotlin.math.pow


class DisplayWeatherActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val appid = "&units=metric&lang=hu&appid=43a9f1572a829eefaf76bb03be055377"
    private var url = "https://api.openweathermap.org/data/2.5/weather?"
    private var forecastUrl= "https://api.openweathermap.org/data/2.5/forecast?"


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.displayweather)

        loading.indeterminateDrawable.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN );
        loading.visibility = View.VISIBLE

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

    private fun setBackGroundAnimation(weather : String)
    {

        runOnUiThread {

            when(weather)
            {
                "Clouds"->{

                    setFontColor(Color.BLACK)
                    bg.setBackgroundResource(R.drawable.clouds_anim)
                    var frameAnim : AnimationDrawable  =  bg.background as AnimationDrawable
                    frameAnim.start()

                }
                "Rain"->{

                    setFontColor(Color.WHITE)
                    bg.setBackgroundResource(R.drawable.rain_anim)
                    var frameAnim : AnimationDrawable  =  bg.background as AnimationDrawable
                    frameAnim.start()
                }
                "Clear"->{

                    setFontColor(Color.WHITE)

                    bg.setBackgroundResource(R.drawable.clear_anim)
                    var frameAnim : AnimationDrawable  =  bg.background as AnimationDrawable
                    frameAnim.start()

                }

            }

        }


    }


    private fun setFontColor(color : Int){


         bg.children

        for(i in 0 until  bg.childCount){

            var child = bg.getChildAt(i)

            if (child != null) {

                if(child is TextView){

                    child.setTextColor(color)
                }
            }
        }




    }

    private fun calculateTempFeel(temp : Double , humidity: Double): Double {
        var farenheit = (temp+32)*(9/5)

        return -42.379 + (2.04901523 * farenheit) + (10.14333127 * humidity) - (0.22475541 * farenheit * humidity) - (6.83783 * 10.0.pow(-3) * farenheit.pow(2)) - (5.481717 * 10.0.pow(-2) * humidity.pow(2)) + (1.22874 * 10.0.pow(-3) * farenheit.pow(2) * humidity) + (8.5282 * 10.0.pow(-4) * farenheit * humidity.pow(2)) - (1.99 * 10.0.pow(-6) * farenheit.pow(2) * humidity.pow(2))
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

                    val desc = weather.getString("main")

                    setBackGroundAnimation(desc)

                    val main = JSONObject(root.getString("main"))
                    val wind = JSONObject(root.getString("wind"))

                    val ret = Weather(
                        name,
                        weather.getString("description"),
                        main.getDouble("temp"),
                        wind.getInt("speed"),
                        main.getDouble("humidity")
                    )
                    val imageIcon = weather.getString("icon")

                    temperature.text = (round(ret.temp * 100) / 100).toString() + "°"
                    var realTemperature = calculateTempFeel(ret.temp, ret.humidity)

                    realTemperature = (realTemperature - 32)/1.8000

                    realTemperature = (round(realTemperature * 100) / 100)

                    println(realTemperature)


                    realtemp.text = "Ennyinek érzed: $realTemperature°"



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

                    forecast(forecastUrl)


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
            @RequiresApi(Build.VERSION_CODES.O)
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                val root = JSONObject(response.body()?.string())
                response.body()?.close()

                val currentDate = Date()

                val localDate = currentDate.toInstant().atZone(systemDefault()).toLocalDate()
                val currentDay = localDate.dayOfMonth
                val dayValue = localDate.dayOfWeek.value
                dayLab.text = dayOfTheWeek(dayValue)


                if(root.getInt("cod") == 200){

                    val list = root.getJSONArray("list")

                    //NextDay
                    var nextDayTemp : Double = 0.0
                    var nextDayMax : Double = 0.0
                    var nextDayMin : Double = 0.0
                    var nextDayCount : Int = 0
                    var nextDayBmp : Bitmap? = null

                    //Two Days
                    var twoDayTemp : Double = 0.0
                    var twoDayMax : Double = 0.0
                    var twoDayMin : Double = 0.0
                    var twoDayCount : Int = 0
                    var twoDayBmp : Bitmap? = null

                    //Three Days
                    var threeDayTemp : Double = 0.0
                    var threeDayMax : Double = 0.0
                    var threeDayMin : Double = 0.0
                    var threeDayCount : Int = 0
                    var threeDayBmp : Bitmap? = null



                    for (i in 0 until list.length()) {
                        val item = list.getJSONObject(i)

                        val dateValue = item.getLong("dt")*1000

                        val date = Date(dateValue)

                        val dateLocalDate = date.toInstant().atZone(systemDefault()).toLocalDate()

                        val day = dateLocalDate.dayOfMonth


                        when (day) {
                            currentDay + 1 -> {

                                nextDayTemp += item.getJSONObject("main").getDouble("temp")
                                nextDayMax += item.getJSONObject("main").getDouble("temp_max")
                                nextDayMin += item.getJSONObject("main").getDouble("temp_min")

                                val imageIcon = JSONObject(item.getJSONArray("weather")[0].toString()).getString("icon")
                                val imageUrl = "http://openweathermap.org/img/wn/$imageIcon@2x.png"
                                val inp: InputStream = URL(imageUrl).openStream()
                                nextDayBmp = BitmapFactory.decodeStream(inp)


                                nextDayCount++

                            }
                            currentDay + 2 -> {

                                twoDayTemp += item.getJSONObject("main").getDouble("temp")
                                twoDayMax += item.getJSONObject("main").getDouble("temp_max")
                                twoDayMin += item.getJSONObject("main").getDouble("temp_min")


                                val imageIcon = JSONObject(item.getJSONArray("weather")[0].toString()).getString("icon")
                                val imageUrl = "http://openweathermap.org/img/wn/$imageIcon@2x.png"
                                val inp: InputStream = URL(imageUrl).openStream()
                                twoDayBmp = BitmapFactory.decodeStream(inp)


                                twoDayCount++

                            }
                            currentDay + 3 -> {

                                threeDayTemp += item.getJSONObject("main").getDouble("temp")
                                threeDayMax += item.getJSONObject("main").getDouble("temp_max")
                                threeDayMin += item.getJSONObject("main").getDouble("temp_min")

                                val imageIcon = JSONObject(item.getJSONArray("weather")[0].toString()).getString("icon")
                                val imageUrl = "http://openweathermap.org/img/wn/$imageIcon@2x.png"
                                val inp: InputStream = URL(imageUrl).openStream()
                                threeDayBmp = BitmapFactory.decodeStream(inp)

                                threeDayCount++

                            }
                        }

                    }




                    runOnUiThread {
                        loading.visibility = View.GONE
                        nextDay.text = dayOfTheWeek(dayValue + 1 )
                        nextDayTempLab.text = (round((nextDayTemp/nextDayCount)*100)/100).toString() + " °"
                        twoDay.text = dayOfTheWeek(dayValue + 2)
                        twoDayTempLab.text = (round((twoDayTemp/twoDayCount)*100)/100).toString() + " °"
                        threeDay.text = dayOfTheWeek(dayValue + 3)
                        threeDayTempLab.text = (round((threeDayTemp/threeDayCount)*100)/100).toString() + " °"

                        nextDayImage.setImageBitmap(nextDayBmp)
                        twoDayImage.setImageBitmap(twoDayBmp)
                        threeDayImage.setImageBitmap((threeDayBmp))
                    }






                }
            }
        })

    }


    private fun dayOfTheWeek(value : Int): String {

        when(value){

            1 ->{
                return "Hétfő"
            }

            2 ->{
                return "Kedd"
            }

            3 ->{
                return "Szerda"
            }

            4 ->{
                return "Csütörtök"
            }

            5 ->{
                return "Péntek"
            }

            6 ->{
                return "Szombat"
            }

            7 ->{
                return "Vasárnap"
            }

        }

        return "NINCS ILYEN NAP!"

    }
}


data class Weather(val place : String , val description : String ,  val temp : Double , val windspeed : Int , val humidity : Double)



