package com.blockeq.stellarwallet.models

import android.content.Context

interface UserSession {
    fun getSessionAsset(): SessionAsset
    fun setSessionAsset(sessionAsset: SessionAsset)
    fun getPin(): String?
    fun setPin(pin: String?)
    fun getFormattedCurrentAssetCode(): String?
    fun getFormattedCurrentAvailableBalance(context: Context): String?
    fun getAvailableBalance(): String?
    fun setMinimumBalance(minimumBalance: MinimumBalance)
    fun getMinimumBalance(): MinimumBalance?
}
