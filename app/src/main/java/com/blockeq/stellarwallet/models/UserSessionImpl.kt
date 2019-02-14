package com.blockeq.stellarwallet.models

class UserSessionImpl : UserSession {

    override fun setMinimumBalance(minimumBalance: MinimumBalance) {
        minimumBalanceSession = minimumBalance
    }

    override fun getMinimumBalance(): MinimumBalance? {
       return minimumBalanceSession
    }

    private var asset : SessionAsset = DefaultAsset()

    override fun getSessionAsset(): SessionAsset {
      return asset
    }

    override fun setSessionAsset(sessionAsset: SessionAsset){
        asset = sessionAsset
    }

    override fun setPin(pin:String?) {
      sessionPin = pin
    }

    private var minimumBalanceSession: MinimumBalance? = null
    private var sessionPin: String? = null

    override fun getPin(): String? {
        return sessionPin
    }
}
