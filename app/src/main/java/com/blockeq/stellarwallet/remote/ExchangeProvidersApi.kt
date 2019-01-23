package com.blockeq.stellarwallet.remote

import com.blockeq.stellarwallet.models.ExchangeApiModel

import retrofit2.Call
import retrofit2.http.GET

interface ExchangeProvidersApi {
    @GET("directory/exchanges?asArray")
    fun exchangeProviders(): Call<List<ExchangeApiModel>>
}
