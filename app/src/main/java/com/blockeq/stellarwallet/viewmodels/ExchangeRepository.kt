package com.blockeq.stellarwallet.viewmodels

import android.app.Application
import android.arch.lifecycle.LiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.models.ExchangeApiModel
import com.blockeq.stellarwallet.models.ExchangeMapper
import com.google.gson.GsonBuilder
import timber.log.Timber

class ExchangeRepository(application: Application) {
    private val appContext = application.applicationContext
    private val exchangeProviderDao: ExchangeDao
//    private val listLiveData: LiveData<List<ExchangeEntity>>
    init {
        val exchangeRoomDatabase = ExchangesRoomDatabase.getDatabase(application)
        exchangeProviderDao = exchangeRoomDatabase!!.exchangeDao()
//        listLiveData = exchangeProviderDao.getAllExchangeProviders()
    }

    fun getAllExchangeProviders(forceRefresh : Boolean = false) : List<ExchangeEntity> {
        if (forceRefresh) {

        } else {
        }
        return exchangeProviderDao.getAllExchangeProviders()
    }

    private fun populateExchangeDatabase(exchanges : List<ExchangeApiModel>) {
        val dao = ExchangesRoomDatabase.getDatabase(appContext)!!.exchangeDao()
        dao.deleteAll()
        dao.insertAll(ExchangeMapper.toExchangeEntities(exchanges.toList()))
        Timber.d("Refreshing exchanges database from remote server")
    }

    /**
     * It will fetch the exchanges and populate the exchanges database.
     */
    private fun refreshExchanges() {
        val queue = Volley.newRequestQueue(appContext)
        // TODO: Use retrofit and dagger
        val request = JsonArrayRequest(Request.Method.GET, Constants.BLOCKEQ_EXCHANGES_URL, null,
                Response.Listener { response ->
                    // display response
                    val gson = GsonBuilder().create()
                    val list = gson.fromJson(response.toString(), Array<ExchangeApiModel>::class.java)

                    if (list != null && list.isNotEmpty()) {
                        populateExchangeDatabase(list.toList())
                    }
                },
                Response.ErrorListener {
                    it.networkResponse
                    Timber.e("Error fetching exchange providers")
                })

        queue.add(request)
    }
//
//    fun insert(exchange: ExchangeEntity) {
//        exchangeProviderDao.insert(exchange)
//    }
}