package com.example.bucksplumbingordering.classes

import android.media.Image
import androidx.camera.core.*
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.json.JSONObject

class QRCodeAnalyzer(private val onQRCodeDetected: (Map<String, Any>) -> Unit) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: return

        try {
            // Convert the ImageProxy to ByteArray and analyze
            val rotationDegrees = image.imageInfo.rotationDegrees
            val bytes = imageToByteArray(mediaImage)
            val source = PlanarYUVLuminanceSource(
                bytes,
                mediaImage.width,
                mediaImage.height,
                0, 0,
                mediaImage.width,
                mediaImage.height,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            // Decode QR code
            val result = reader.decode(binaryBitmap)
            val qrCodeData = result.text
            println("QR Code detected: $qrCodeData") // Log QR code content for debugging

            // Parse JSON
            val jsonObject = JSONObject(qrCodeData)
            val parsedData = mapOf(
                "item_number" to jsonObject.getString("item_number"),
                "item_name" to jsonObject.getString("item_name"),
                "max_quantity" to jsonObject.getInt("max_quantity") // Ensure integer for max_quantity
            )

            // Pass the parsed data to onQRCodeDetected
            onQRCodeDetected(parsedData)
        } catch (e: Exception) {
            println("Error during QR code analysis: ${e.message}") // Log errors if decoding fails
        } finally {
            image.close() // Ensure the image is closed
        }
    }

    private fun imageToByteArray(image: Image): ByteArray {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return bytes
    }
}


//    private fun handleQRCodeData(qrCodeData: String) {
//        try {
//            // Parse QR code data as JSON
//            val jsonObject = JSONObject(qrCodeData)
//
//            // Access data from the JSON object
//            val item_number = jsonObject.getString("item_number")
//            val item_name = jsonObject.getString("item_name")
//            val max_quantity = jsonObject.getInt("max_quantity")
//
//            // Use the parsed data in your app as needed
//            println("Parsed data: $item_number, $item_name, $max_quantity")
//
//        } catch (e: Exception) {
//            // Handle errors if the QR code data is not in valid JSON format
//            println("Error parsing JSON: ${e.message}")
//        }
//    }
//}
