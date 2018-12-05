package com.blockeq.stellarwallet.networking.api

import com.blockeq.stellarwallet.responses.SupportedAssetResponse
import retrofit2.Call
import retrofit2.http.GET

interface SupportedAssetAPI {

    @GET("/directory/assets")
    fun requestSupportedAssets() : Call<Map<String, SupportedAssetResponse>>

}
