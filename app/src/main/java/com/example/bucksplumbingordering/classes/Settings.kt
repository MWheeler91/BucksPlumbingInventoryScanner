package com.example.bucksplumbingordering.classes

import android.content.Context

data class Settings(var companyName: String = "", var email: String = "")

class SettingsManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    fun saveCompanyName(name: String) {
        sharedPreferences.edit().putString("company_name", name).apply()
    }

    fun getCompanyName(): String? {
        return sharedPreferences.getString("company_name", null)
    }

    fun saveEmail(email: String) {
        sharedPreferences.edit().putString("email_address", email).apply()
    }

    fun getEmail(): String? {
        return sharedPreferences.getString("email_address", null)
    }
}