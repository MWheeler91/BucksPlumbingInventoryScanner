package com.example.bucksplumbingordering.classes

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Order(
    val items: List<Item>, // List of items in the order
)

class OrderManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("order_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveOrder(order: Order) {
        val jsonString = gson.toJson(order)
        sharedPreferences.edit().putString("current_order", jsonString).apply()
    }

    fun getOrder(): Order? {
        val jsonString = sharedPreferences.getString("current_order", null)
        return if (jsonString != null) {
            val type = object : TypeToken<Order>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            null
        }
    }
}
