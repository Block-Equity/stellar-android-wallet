package com.blockeq.stellarwallet.models

class GooglePlayApp(private val packageName: String) {
    fun getUrl() : String {
        return "https://play.google.com/store/apps/details?id=$packageName"
    }

    fun getDeepLink():String {
        return "market://details?id=$packageName"
    }
}
