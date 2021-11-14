package com.example.myyelp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "eUSf6-87y25MXTdQJmztMJ-sAyWdn0B2VPsDS2iY51rX_U2H_BwK98rxwafe6nUvYBO-ma-GpxaOrEsH89VBW4wRksI1huRllpK7bBvFgNbd_3169WHNEZXYg_ePYXYx"
class MainActivity : AppCompatActivity() {
    private lateinit var rvRestaurants: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val restaurants = mutableListOf<YelpRestaurant>()
        val adapter = RestaurantsAdapter(this, restaurants, object: RestaurantsAdapter.OnClickListener {
            //make a pop up window when someone clicks on a restaurant to show their phone number, whether they are open currently, and types of transactions(pickup, delivery, etc)
            override fun onItemClick(position: Int) {
                val restaurant = restaurants[position]
                var phone = "Not Available"
                if (restaurant.phone.isNotEmpty()) phone = restaurant.phone
                var open_now = "No"
                if (restaurant.is_closed == "false") open_now = "Yes"
                var transactions = ""
                for (item in restaurant.transactions) transactions += ("$item, ")
                val infoBuilder = AlertDialog.Builder(this@MainActivity)
                infoBuilder.apply {
                    setTitle(restaurant.name)
                    setMessage("Phone: ${phone}" + "\n" +
                            "Open Now: $open_now" + "\n" +
                            "Transactions: ${transactions}")
                    setPositiveButton("Exit", null)
                    val iDialog = infoBuilder.create()
                    iDialog.show()
                }
            }

        })
        rvRestaurants = findViewById(R.id.rvRestaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        val yelpService = retrofit.create(YelpService::class.java)
        yelpService.searchRestaurants("Bearer $API_KEY","Avocado Toast", "New York").enqueue(object : Callback<YelpSearchResult> {
            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onresponse $response")
                val body = response.body()
                if (body == null) {
                    Log.w(TAG, "Did not receive valid reponse body from Yelp API...")
                    return
                }
                restaurants.addAll(body.restaurants)
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                Log.i(TAG, "onFailure $t")
            }



        })
    }
}