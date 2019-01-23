package com.blockeq.stellarwallet.mvvm.exchange

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.models.ExchangeApiModel
import com.blockeq.stellarwallet.models.ExchangeMapper
import com.blockeq.stellarwallet.remote.ExchangeProvidersApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import shadow.com.google.gson.GsonBuilder
import timber.log.Timber

class ExchangeRepository(application: Application) {
    private val appContext = application.applicationContext
    private val exchangeDao: ExchangeDao
    private val listLiveData: MutableLiveData<List<ExchangeEntity>>
    init {
        val exchangeRoomDatabase = ExchangesRoomDatabase.getDatabase(application)
        exchangeDao = exchangeRoomDatabase.exchangeDao()
        listLiveData = MutableLiveData()
    }

    fun getAllExchangeProviders(forceRefresh : Boolean = false) : LiveData<List<ExchangeEntity>> {
        if (forceRefresh || exchangeDao.getAllExchangeProviders().isEmpty()) {
            refreshExchanges()
        } else {
            listLiveData.postValue(exchangeDao.getAllExchangeProviders())
        }
        return listLiveData
    }

    private fun populateExchangeDatabase(exchanges : List<ExchangeApiModel>) {
        val dao = ExchangesRoomDatabase.getDatabase(appContext).exchangeDao()
        dao.deleteAll()
        dao.insertAll(ExchangeMapper.toExchangeEntities(exchanges.toList()))
        Timber.d("Refreshing exchanges database from remote server")
    }

    private fun refreshExchanges() {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.blockeq.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        retrofit.create(ExchangeProvidersApi::class.java).exchangeProviders().enqueue(object : Callback<List<ExchangeApiModel>>{
            override fun onResponse(call: Call<List<ExchangeApiModel>>, response: retrofit2.Response<List<ExchangeApiModel>>) {
                val list = response.body()
                if (list != null && list.isNotEmpty()) {
                    Timber.v("Fetched and updated {${list.size}} exchange providers")
                    populateExchangeDatabase(list.toList())
                    listLiveData.postValue(exchangeDao.getAllExchangeProviders())
                }
            }

            override fun onFailure(call: Call<List<ExchangeApiModel>>, t: Throwable) {
                Timber.e("Error fetching exchange providers")
            }

        })
    }

    fun getExchange(address : String) : ExchangeEntity {
        return exchangeDao.getExchangeProvider(address)
    }
//
//    fun insert(exchange: ExchangeEntity) {
//        exchangeDao.insert(exchange)
//    }
}