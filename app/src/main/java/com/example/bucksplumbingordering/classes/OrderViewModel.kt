package com.example.bucksplumbingordering.classes

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class OrderViewModel : ViewModel() {
    var orderItems = mutableStateListOf<Item>()

    fun addItem(item: Item) {
        orderItems.add(item)
    }

    fun removeItem(item: Item) {
        orderItems.remove(item)
    }

    fun clearItems() {
        orderItems.clear()
    }

    fun setItems(items: List<Item>) {
        orderItems.clear() // Clear current items
        orderItems.addAll(items) // Add new items
    }
}
