package com.blockeq.stellarwallet.vmodels.exchange

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
import com.google.gson.GsonBuilder
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
                        listLiveData.postValue(exchangeDao.getAllExchangeProviders())
                    }
                },
                Response.ErrorListener {
                    it.networkResponse
                    Timber.e("Error fetching exchange providers")
                })

        queue.add(request)
    }

    fun getExchange(address : String) : ExchangeEntity {
        return exchangeDao.getExchangeProvider(address)
    }
//
//    fun insert(exchange: ExchangeEntity) {
//        exchangeDao.insert(exchange)
//    }
}