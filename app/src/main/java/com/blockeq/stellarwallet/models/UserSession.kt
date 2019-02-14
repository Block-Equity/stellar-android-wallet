package com.blockeq.stellarwallet.models


interface UserSession {
    fun getSessionAsset(): SessionAsset
    fun setSessionAsset(sessionAsset: SessionAsset)
    fun getPin(): String?
    fun setPin(pin: String?)
    fun setMinimumBalance(minimumBalance: MinimumBalance)
    fun getMinimumBalance(): MinimumBalance?
}
