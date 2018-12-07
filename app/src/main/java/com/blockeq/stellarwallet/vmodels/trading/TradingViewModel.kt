package com.blockeq.stellarwallet.vmodels.trading

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.widget.Toast
import com.blockeq.stellarwallet.models.*
import com.blockeq.stellarwallet.remote.Horizon
import org.stellar.sdk.responses.OrderBookResponse
import timber.log.Timber
import java.util.*
import android.os.AsyncTask



class TradingViewModel(application: Application) : AndroidViewModel(application) {
    val tradingPair : MutableLiveData<TradingPair> = MutableLiveData()
    val orderBook : MutableLiveData<List<OrderBook>> = MutableLiveData()
    var orderBookList : ArrayList<OrderBook> = ArrayList()
//    var refresh : MutableLiveData<Boolean> = MutableLiveData()
    init {
        tradingPair.observeForever {
            if (it != null) {
                AsyncTask.execute {
                    loadOrderBook(it.buying, it.selling)
                }
            }
        }

//        refresh.observeForever{
//            orderBook.postValue(orderBookList)
//        }
    }

    private fun loadOrderBook(buying: DataAsset, selling: DataAsset) {
        Horizon.getOrderBook(object : Horizon.OnOrderBookListener {
            override fun onOrderBook(asks: Array<OrderBookResponse.Row>, bids: Array<OrderBookResponse.Row>) {
                val orderBooks = ArrayList<OrderBook>()
                val orderBooksTitle = OrderBook(type = OrderBookAdapterTypes.TITLE)
                val buyOffer = OrderBookStickyHeader(type = OrderBookAdapterTypes.BUY_HEADER)
                val sellOffer = OrderBookStickyHeader(type = OrderBookAdapterTypes.SELL_HEADER)
                val subheader = OrderBook(type = OrderBookAdapterTypes.SUBHEADER)
                orderBooks.add(orderBooksTitle)
                orderBooks.add(buyOffer)
                orderBooks.add(subheader)
                var id = 1
                bids.forEach {
                    val item = OrderBook(id, Date(), it.price.toFloat(), it.amount.toFloat() / it.price.toFloat(), it.amount.toFloat(), OrderBookAdapterTypes.ITEM)
                    orderBooks.add(item)
                    id++
                }
                orderBooks.add(sellOffer)
                orderBooks.add(subheader)
                asks.forEach {
                    val item = OrderBook(id, Date(), it.price.toFloat(), it.amount.toFloat(), it.price.toFloat() * it.amount.toFloat(), OrderBookAdapterTypes.ITEM)
                    orderBooks.add(item)
                    id++

                }
                Timber.d("loading items value size %s", orderBooks.size)
//                orderBookList = orderBooks
                            orderBook.postValue(orderBookList)

            }

            override fun onFailed(errorMessage: String) {
                //TODO what to send to the view?
                Toast.makeText(getApplication(), "Failed" + errorMessage, Toast.LENGTH_SHORT).show()
            }

        }, buying, selling)
    }
}