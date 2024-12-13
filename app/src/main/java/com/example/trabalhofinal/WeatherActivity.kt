package com.example.trabalhofinal;

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    var WEATHER_URL = ""
    var WEATHER_URL_16 = ""
    var API_KEY = "bef266cc01b74afe8335e6ba6221971c"
    var DayWeek = ""
    var Day = ""
    var Month = ""
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        val calendar = Calendar.getInstance()

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        var i = 0

        while(i < 7){

            val today = calendar.time
            val DayWeekOutFormat = SimpleDateFormat("EEEE")
            DayWeek = DayWeekOutFormat.format(today)
            val DayOutFormat = SimpleDateFormat("dd")
            Day = DayOutFormat.format(today)
            val MonthOutFormat = SimpleDateFormat("MM")
            Month = MonthOutFormat.format(today)
            val Date = Day + "/" + Month
            if(DayWeek == "domingo")
                DayWeek = "Sun"
            else if(DayWeek == "segunda-feira")
                DayWeek = "Mon"
            else if(DayWeek == "terça-feira")
                DayWeek = "Tue"
            else if(DayWeek == "quarta-feira")
                DayWeek = "Wed"
            else if(DayWeek == "quinta-feira")
                DayWeek = "Thu"
            else if(DayWeek == "sexta-feira")
                DayWeek = "Fri"
            else if(DayWeek == "sábado")
                DayWeek = "Sat"

            if(i == 0){
                findViewById<TextView>(R.id.info_date0).text = Date
            }
            else if(i == 1) {
                findViewById<TextView>(R.id.info_day1).text = DayWeek
                findViewById<TextView>(R.id.info_date1).text = Date
            }
            else if(i == 2) {
                findViewById<TextView>(R.id.info_day2).text = DayWeek
                findViewById<TextView>(R.id.info_date2).text = Date
            }
            else if(i == 3) {
                findViewById<TextView>(R.id.info_day3).text = DayWeek
                findViewById<TextView>(R.id.info_date3).text = Date
            }
            else if(i == 4) {
                findViewById<TextView>(R.id.info_day4).text = DayWeek
                findViewById<TextView>(R.id.info_date4).text = Date
            }
            else if(i == 5) {
                findViewById<TextView>(R.id.info_day5).text = DayWeek
                findViewById<TextView>(R.id.info_date5).text = Date
            }
            else if(i == 6) {
                findViewById<TextView>(R.id.info_day6).text = DayWeek
                findViewById<TextView>(R.id.info_date6).text = Date
            }

            calendar.add(Calendar.DAY_OF_YEAR, 1)
            i += 1
        }

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val buttonGetWeather = findViewById<Button>(R.id.btGetWeather)
        buttonGetWeather.setOnClickListener{
            Toast.makeText(this, "Getting weather data", Toast.LENGTH_LONG).show()

            //uncomment after the next slide
            obtainLocation()
        }
    }

    private fun obtainLocation() {

        WEATHER_URL =
            "https://api.weatherbit.io/v2.0/current?" +
                    "lat=" + latitude +
                    "&lon=" + longitude +
                    "&key=" + API_KEY
        WEATHER_URL_16 =
            "https://api.weatherbit.io/v2.0/forecast/daily?" +
                    "lat=" + latitude +
                    "&lon=" + longitude +
                    "&key=" + API_KEY

        // this function will, fetch data from URL
        Log.d("Weather_URL",WEATHER_URL)
        getWeatherInformation()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var permission_granted = true
        for (i in 0 until permissions.size) {
            val grantResult = grantResults[i]
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                permission_granted = false
            }
        }
        if( permission_granted) {
            getWeatherInformation()
        }
    }

    fun getWeatherInformation() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)

        // Request a string response  from the provided URL.
        val stringReq = StringRequest(Request.Method.GET, WEATHER_URL,
            Response.Listener<String> { response ->
                showWeatherInformation(response)
            },
            // In case of any error
            Response.ErrorListener {
                Toast.makeText(getApplicationContext(), "Could not get weather information", Toast.LENGTH_SHORT).show();
            })
        queue.add(stringReq)
        val stringReq16 = StringRequest(Request.Method.GET, WEATHER_URL_16,
            Response.Listener<String> { response ->
                showWeatherInformation16(response) //TO DO: horizontalScroll on layout
            },
            // In case of any error
            Response.ErrorListener {
                Toast.makeText(getApplicationContext(), "Could not get weather information", Toast.LENGTH_SHORT).show();
            })
        queue.add(stringReq16)


    }

    fun showWeatherInformation(jsonWeather: String) {
        val obj = JSONObject(jsonWeather)
        val arr = obj.getJSONArray("data")  // weather info is in the array called data
        val objData = arr.getJSONObject(0)  // get position 0 of the array
        findViewById<TextView>(R.id.info_temperature).text = objData.getString("temp") + "ºC"

        //weather description
        val objWeather = objData.getJSONObject("weather")
        findViewById<TextView>(R.id.info_description).text = objWeather.getString("description")

        //location
        findViewById<TextView>(R.id.info_location).text = objData.getString("city_name")

        //humidity
        findViewById<TextView>(R.id.info_Humidity).text = objData.getString("rh")
        //pressure
        findViewById<TextView>(R.id.info_Pressure).text = objData.getString("pres")
        //Solar Radiation
        findViewById<TextView>(R.id.info_SolarRadiation).text = objData.getString("solar_rad")
        //weather icon
        val imageIconCode = objWeather.getString("icon")
        val drawableResourceID = this.resources.getIdentifier(imageIconCode, "drawable", this.packageName)
        findViewById<ImageView>(R.id.info_image).setImageResource(drawableResourceID)
    }

    fun showWeatherInformation16(jsonWeather: String) {
        val obj = JSONObject(jsonWeather)
        val arr = obj.getJSONArray("data")  // weather info is in the array called data
        val objData = arr.getJSONObject(0)  // get position 0 of the array
        findViewById<TextView>(R.id.info_temperature_0_Max_Value).text = objData.getString("max_temp") + "º"

        findViewById<TextView>(R.id.info_temperature_0_Min_Value).text = objData.getString("min_temp") + "º"

        val objWeather = objData.getJSONObject("weather")
        //weather icon
        val imageIconCode = objWeather.getString("icon")
        val drawableResourceID = this.resources.getIdentifier(imageIconCode, "drawable", this.packageName)
        findViewById<ImageView>(R.id.info_image0).setImageResource(drawableResourceID)

        val objData1 = arr.getJSONObject(1)

        findViewById<TextView>(R.id.info_temperature_1_Max_Value).text = objData1.getString("max_temp") + "º"
        findViewById<TextView>(R.id.info_temperature_1_Min_Value).text = objData1.getString("min_temp") + "º"

        val objWeather1 = objData1.getJSONObject("weather")
        val imageIconCode1 = objWeather1.getString("icon")
        val drawableResourceID1 = this.resources.getIdentifier(imageIconCode1, "drawable", this.packageName)
        findViewById<ImageView>(R.id.info_image1).setImageResource(drawableResourceID1)

        val objData2 = arr.getJSONObject(2)

        findViewById<TextView>(R.id.info_temperature_2_Max_Value).text = objData2.getString("max_temp") + "º"
        findViewById<TextView>(R.id.info_temperature_2_Min_Value).text = objData2.getString("min_temp") + "º"

        val objWeather2 = objData2.getJSONObject("weather")
        val imageIconCode2 = objWeather2.getString("icon")
        val drawableResourceID2 = this.resources.getIdentifier(imageIconCode2, "drawable", this.packageName)
        findViewById<ImageView>(R.id.info_image2).setImageResource(drawableResourceID2)

        val objData3 = arr.getJSONObject(3)

        findViewById<TextView>(R.id.info_temperature_3_Max_Value).text = objData3.getString("max_temp") + "º"
        findViewById<TextView>(R.id.info_temperature_3_Min_Value).text = objData3.getString("min_temp") + "º"

        val objWeather3 = objData3.getJSONObject("weather")
        val imageIconCode3 = objWeather3.getString("icon")
        val drawableResourceID3 = this.resources.getIdentifier(imageIconCode3, "drawable", this.packageName)
        findViewById<ImageView>(R.id.info_image3).setImageResource(drawableResourceID3)

        val objData4 = arr.getJSONObject(4)

        findViewById<TextView>(R.id.info_temperature_4_Max_Value).text = objData4.getString("max_temp") + "º"
        findViewById<TextView>(R.id.info_temperature_4_Min_Value).text = objData4.getString("min_temp") + "º"

        val objWeather4 = objData4.getJSONObject("weather")
        val imageIconCode4 = objWeather4.getString("icon")
        val drawableResourceID4 = this.resources.getIdentifier(imageIconCode4, "drawable", this.packageName)
        findViewById<ImageView>(R.id.info_image4).setImageResource(drawableResourceID4)

        val objData5 = arr.getJSONObject(5)

        findViewById<TextView>(R.id.info_temperature_5_Max_Value).text = objData5.getString("max_temp") + "º"
        findViewById<TextView>(R.id.info_temperature_5_Min_Value).text = objData5.getString("min_temp") + "º"

        val objWeather5 = objData5.getJSONObject("weather")
        val imageIconCode5 = objWeather5.getString("icon")
        val drawableResourceID5 = this.resources.getIdentifier(imageIconCode5, "drawable", this.packageName)
        findViewById<ImageView>(R.id.info_image5).setImageResource(drawableResourceID5)

        val objData6 = arr.getJSONObject(6)

        findViewById<TextView>(R.id.info_temperature_6_Max_Value).text = objData6.getString("max_temp") + "º"
        findViewById<TextView>(R.id.info_temperature_6_Min_Value).text = objData6.getString("min_temp") + "º"

        val objWeather6 = objData6.getJSONObject("weather")
        val imageIconCode6 = objWeather6.getString("icon")
        val drawableResourceID6 = this.resources.getIdentifier(imageIconCode6, "drawable", this.packageName)
        findViewById<ImageView>(R.id.info_image6).setImageResource(drawableResourceID6)
    }



}
