package com.example.bucksplumbingordering

import android.content.Intent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import android.widget.Toast
import com.example.bucksplumbingordering.classes.Item
import com.example.bucksplumbingordering.classes.Order
import com.example.bucksplumbingordering.classes.OrderManager
import com.example.bucksplumbingordering.classes.OrderViewModel
import com.example.bucksplumbingordering.classes.QRCodeAnalyzer
import com.example.bucksplumbingordering.classes.SettingsManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun OrderCompose(settingsManager: SettingsManager) {
    // Get an instance of OrderViewModel using viewModel() function
    val viewModel: OrderViewModel = viewModel()
    val context = LocalContext.current
    val orderManager = remember { OrderManager(context) }

    // Load existing order when the composable is first called
    val existingOrder = orderManager.getOrder()
    existingOrder?.let {
        viewModel.setItems(it.items) // Set existing items to the ViewModel
    }

    var isCameraOpen by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var scannedItemData by remember { mutableStateOf<Map<String, Any>?>(null) }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    viewModel.clearItems() // Clear the order list using ViewModel
                    orderManager.saveOrder(Order(viewModel.orderItems)) // Save empty order
                }) { Text("Clear") }
                Button(onClick = {
                    submitOrder(context, orderItems = viewModel.orderItems, settingsManager = settingsManager)
                }) { Text("Submit") }
                Button(onClick = { isCameraOpen = true }) { Text("Scan") }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 64.dp, // Adjust this if necessary
                    bottom = paddingValues.calculateBottomPadding()
                )
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Item Number", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp)
                Text("Item Name", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp)
                Text("Ordered Amount", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp)
                Text("Delete", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // LazyColumn for items
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(viewModel.orderItems) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.itemNumber)
                        Text(item.itemName)
                        Text(item.orderedAmount.toString())

                        // Add delete button (small red X)
                        IconButton(onClick = {
                            viewModel.removeItem(item) // Remove item from ViewModel
                            orderManager.saveOrder(Order(viewModel.orderItems)) // Save updated order
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Delete Item", tint = Color.Red)
                        }
                    }
                }
            }
        }

        // Camera and scanned item popup logic remains unchanged
        if (isCameraOpen) {
            CameraPopup(
                isOpen = isCameraOpen,
                onDismiss = { isCameraOpen = false },
                onQRCodeDetected = { data ->
                    scannedItemData = data
                    isCameraOpen = false
                    showDialog = true
                }
            )
        }

        // Show scanned item data popup
        if (showDialog && scannedItemData != null) {
            ScannedItemPopup(
                itemData = scannedItemData ?: emptyMap(),
                onDismiss = { showDialog = false },
                onAddToOrder = { newItem ->
                    viewModel.addItem(newItem) // Add item to ViewModel
                    orderManager.saveOrder(Order(viewModel.orderItems)) // Save updated order
                    showDialog = false // Close the dialog after adding the item
                }
            )
        }
    }
}

@Composable
fun CameraPopup(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onQRCodeDetected: (Map<String, Any>) -> Unit
) {
    if (isOpen) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                elevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview { qrCodeData ->
                        onQRCodeDetected(qrCodeData)
                        onDismiss() // Close the popup after detecting the QR code
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(onQRCodeDetected: (Map<String, Any>) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                    cameraProvider.unbindAll()

                    val preview = Preview.Builder().build()
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val qrCodeAnalyzer = QRCodeAnalyzer { qrCodeData ->
                        onQRCodeDetected(qrCodeData) // Trigger callback with parsed data
                    }

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), qrCodeAnalyzer)
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                    preview.setSurfaceProvider(this.surfaceProvider)
                }, ContextCompat.getMainExecutor(context))
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}


@Composable
fun ScannedItemPopup(
    itemData: Map<String, Any>,
    onDismiss: () -> Unit,
    onAddToOrder: (Item) -> Unit // Callback to add the item
) {
    var orderedAmount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") } // State for error messages

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
//            color = customBackgroundColor
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Item Number: ${itemData["item_number"]}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp)
                Text("Item Name: ${itemData["item_name"]}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp)
                Text("Max Quantity: ${itemData["max_quantity"]}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp)

                OutlinedTextField(
                    value = orderedAmount,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            orderedAmount = newValue
                            errorMessage = "" // Clear error message on new input
                        }
                    },
                    label = { Text("Order Quantity", color = MaterialTheme.colorScheme.onBackground) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier.fillMaxWidth()

                )

                // Display error message if any
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = {
                            // Validation checks
                            val quantity = orderedAmount.toIntOrNull()
                            val maxQuantity = (itemData["max_quantity"] as? Int) ?: 0

                            when {
                                quantity == null || quantity <= 0 -> {
                                    errorMessage = "Please enter a valid quantity."
                                }
                                quantity > maxQuantity -> {
                                    errorMessage = "Quantity cannot exceed max quantity of $maxQuantity."
                                }
                                else -> {
                                    // Create the item and call onAddToOrder
                                    val itemNumber = itemData["item_number"] as String
                                    val itemName = itemData["item_name"] as String

                                    val newItem = Item(itemName, itemNumber, maxQuantity, quantity) // Create your item class instance
                                    onAddToOrder(newItem) // Call the callback with the new item
                                    onDismiss() // Dismiss the dialog
                                }
                            }
                        }
                    ) {
                        Text("Add To Order")
                    }
                }
            }
        }
    }
}


// Retrieve the order list from SharedPreferences
private fun getSavedOrder(context: Context): List<Item> {
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val jsonOrder = sharedPreferences.getString("saved_order", null)

    return if (jsonOrder != null) {
        val type = object : TypeToken<List<Item>>() {}.type
        Gson().fromJson(jsonOrder, type)
    } else {
        emptyList() // Return an empty list if no order is saved
    }
}

// Create a formatted order summary
private fun createOrderDetails(orderList: List<Item>): String {
    val orderDetailsBuilder = StringBuilder()
    orderDetailsBuilder.append("Order Summary:\n\n")

    for (item in orderList) {
        orderDetailsBuilder.append("Item Number: ${item.itemNumber} - ${item.itemName} - Quantity: ${item.orderedAmount}\n")
    }

    return orderDetailsBuilder.toString()
}

// Function to retrieve the order and submit it via email
fun submitOrder(context: Context, orderItems: List<Item>, settingsManager: SettingsManager) {
    // Get the saved email address
    val recipientEmail = settingsManager.getEmail()
    if (recipientEmail.isNullOrEmpty()) {
        // Handle case where email is not set
        Toast.makeText(context, "Email address not set in settings", Toast.LENGTH_SHORT).show()
        return
    }

    // Format the order details
    val orderDetails = orderItems.joinToString(separator = "\n") { item ->
        "${item.itemNumber}: ${item.itemName} - Quantity: ${item.orderedAmount}"
    }

    // Create the email intent
    val emailIntent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
        putExtra(Intent.EXTRA_SUBJECT, "Order Summary")
        putExtra(Intent.EXTRA_TEXT, orderDetails)
    }

    // Launch the intent without a chooser
    context.startActivity(emailIntent)
}

