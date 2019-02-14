package com.blockeq.stellarwallet.models

import com.google.gson.annotations.SerializedName

data class Diagnostic(@SerializedName("fields") val values: Values)

data class Values(
        @SerializedName("App Version") val app_version: String,
        @SerializedName("Device Hardware") val device_hardware: String,
        @SerializedName("Locale") val locale: String,
        @SerializedName("Platform") val platform: String,
        @SerializedName("Public Wallet Address") val public_wallet_address: String,
        @SerializedName("Summary") val summary : String,
        @SerializedName("Used Passphrase") val used_passphrase: Boolean,
        @SerializedName("Wallet Creation Method") val wallet_creation_method: String)