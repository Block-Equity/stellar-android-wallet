package com.blockeq.stellarwallet.fragments.tabs

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.adapters.OrderBooksAdapter
import com.blockeq.stellarwallet.interfaces.OnUpdateTradingCurrencies
import com.blockeq.stellarwallet.models.*
import com.blockeq.stellarwallet.remote.Horizon
import kotlinx.android.synthetic.main.fragment_tab_order_book.*
import org.stellar.sdk.responses.OrderBookResponse
import timber.log.Timber
import java.util.*
import com.brandongogetap.stickyheaders.StickyLayoutManager

class OrderBookTabFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, OnUpdateTradingCurrencies {

    private var orderBooks = mutableListOf<OrderBook>()
    private lateinit var orderBooksAdapter: OrderBooksAdapter
    private var buyingAsset : DataAsset? = null
    private var sellingAsset : DataAsset? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_order_book, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dividerItemDecoration = DividerItemDecoration(context,
                LinearLayoutManager(context).orientation)
        orderBookRv.addItemDecoration(dividerItemDecoration)
        swipeRefresh.setOnRefreshListener(this)

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Timber.d("setUserVisibleHint %s", isVisibleToUser)
        if (isVisibleToUser && orderBookRv != null && buyingAsset != null && sellingAsset != null) {
            updateList(buyingAsset!!.code, sellingAsset!!.code)
            Timber.d("setUserVisibleHint > Refreshing")
            onRefresh()

        }
    }

    override fun onRefresh() {
        if (!isAdded || isDetached || !isVisible) {
            Timber.d("onRefresh failed fragment not ready")
            return
        }

        Timber.d("buyingAsset %s sellingAsset %s", buyingAsset, sellingAsset)

        if (buyingAsset != null && sellingAsset != null) {
            loadOrderBook(buyingAsset!!, sellingAsset!!)
        }

    }

    private fun loadOrderBook(buy : DataAsset, sell : DataAsset) {
        Horizon.getOrderBook(object:Horizon.OnOrderBookListener {
            override fun onOrderBook(asks: Array<OrderBookResponse.Row>, bids: Array<OrderBookResponse.Row>) {
                orderBooks.clear()
                val orderBooksTitle = OrderBook(type = OrderBookAdapterTypes.TITLE)
                val buyOffer = OrderBookStickyHeader(type = OrderBookAdapterTypes.BUY_HEADER)
                val sellOffer = OrderBookStickyHeader(type = OrderBookAdapterTypes.SELL_HEADER)
                val subheader = OrderBook(type = OrderBookAdapterTypes.SUBHEADER)
                orderBooks.add(orderBooksTitle)
                orderBooks.add(buyOffer)
                orderBooks.add(subheader)
                var id = 1
                bids.forEach {
                    val item = OrderBook(id, Date(), it.price.toFloat(), it.amount.toFloat() / it.price.toFloat() , it.amount.toFloat(), OrderBookAdapterTypes.ITEM)
                    orderBooks.add(item)
                    id++
                }
                orderBooks.add(sellOffer)
                orderBooks.add(subheader)
                asks.forEach {
                    val item = OrderBook(id, Date(), it.price.toFloat(),  it.amount.toFloat(), it.price.toFloat() * it.amount.toFloat(), OrderBookAdapterTypes.ITEM)
                    orderBooks.add(item)
                    id++

                }
                Timber.d("loading order book complete items %s", orderBooks.size)

                Handler(Looper.getMainLooper()).post {
                    orderBooksAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailed(errorMessage: String) {
                Timber.d("failed to load the order book %s", errorMessage)
            }

        }, buy, sell)

        if (swipeRefresh != null) {
            swipeRefresh.isRefreshing = false
            Toast.makeText(context, getText(R.string.refreshed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun updateTradingCurrencies(currencyCodeFrom: SelectionModel, currencyCodeTo: SelectionModel) {
        val buying =  AssetUtil.toDataAssetFrom(currencyCodeFrom)
        val sell =  AssetUtil.toDataAssetFrom(currencyCodeTo)

        buyingAsset = buying
        sellingAsset = sell

        if (orderBookRv != null) {
            updateList(buyingAsset!!.code, sellingAsset!!.code)
        }
    }

    private fun updateList(codeFrom: String, codeTo: String) {
        if (!::orderBooksAdapter.isInitialized) {
            orderBooksAdapter = OrderBooksAdapter(orderBooks, codeFrom, codeTo, context)
            orderBookRv.adapter = orderBooksAdapter

            val layout = StickyLayoutManager(context, orderBooksAdapter)
            // this will solve the compilation issue Type Mismatch
            if (layout is LinearLayoutManager) {
                orderBookRv.layoutManager = layout
            }
        }

        Timber.d("updateTradingCurrencies %s %s", codeFrom, codeTo)
        orderBooksAdapter.setCurrencies(codeFrom, codeTo)
        orderBooksAdapter.notifyDataSetChanged()

    }
}