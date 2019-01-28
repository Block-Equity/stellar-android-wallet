package com.blockeq.stellarwallet.models

class SessionAssetImpl(var type:String, var name:String, var issuer:String) : SessionAsset {
    override fun getAssetCode(): String {
      return type
    }

    override fun getAssetName(): String {
      return name
    }

    override fun getAssetIssuer(): String {
       return issuer
    }
}
