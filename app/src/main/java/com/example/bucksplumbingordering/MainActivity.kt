package com.example.bucksplumbingordering

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bucksplumbingordering.ui.theme.BucksPlumbingOrderingTheme
import androidx.compose.ui.window.Dialog
import com.example.bucksplumbingordering.classes.SettingsManager


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var settingsManager: SettingsManager
    private var showDialog by mutableStateOf(false) // State for dialog visibility
    private var companyName by mutableStateOf("")

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsManager = SettingsManager(this)

        companyName = settingsManager.getCompanyName() ?: ""


        setContent {
            BucksPlumbingOrderingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text(companyName) },
                                navigationIcon = {
                                    IconButton(onClick = { showDialog = true }) { // Set dialog visibility to true
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                }
                            )
                        },
                    ) { paddingValues ->
                        OrderCompose(settingsManager)
                    }

                    // Show the settings dialog if needed
                    if (showDialog) {
                        SettingsDialog(settingsManager) { showDialog = false } // Pass a callback to close the dialog
                    }
                }
            }
        }
    }
}


@Composable
fun SettingsDialog(settingsManager: SettingsManager, onDismiss: () -> Unit) {
    var companyName by remember { mutableStateOf(settingsManager.getCompanyName() ?: "") }
    var email by remember { mutableStateOf(settingsManager.getEmail() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        settingsManager.saveCompanyName(companyName)
                        settingsManager.saveEmail(email)
                        onDismiss() // Call the dismiss function
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}



