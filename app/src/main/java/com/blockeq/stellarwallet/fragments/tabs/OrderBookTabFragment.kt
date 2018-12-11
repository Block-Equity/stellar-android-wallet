package com.blockeq.stellarwallet.fragments.tabs

import android.content.Context
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
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.adapters.OrderBooksAdapter
import com.blockeq.stellarwallet.interfaces.OnUpdateOrderBook
import com.blockeq.stellarwallet.models.*
import com.blockeq.stellarwallet.utils.MixedTypes
import com.brandongogetap.stickyheaders.StickyLayoutManager
import kotlinx.android.synthetic.main.fragment_tab_order_book.*
import org.stellar.sdk.responses.OrderBookResponse
import timber.log.Timber
import java.util.*

class OrderBookTabFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, OnUpdateOrderBook {
    override fun updateOrderBook(sellingCode: String, buyingCode: String, asks: Array<OrderBookResponse.Row>, bids: Array<OrderBookResponse.Row>) {
        loadOrderBook(sellingCode, buyingCode, asks, bids)
    }

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
//            loadOrderBook(buyingAsset!!, sellingAsset!!)
        }
    }

    private fun loadOrderBook(sellingCode: String, buyingCode: String, asks: Array<OrderBookResponse.Row>, bids: Array<OrderBookResponse.Row>) {
        orderBooks.clear()
        val orderBooksTitle = OrderBook(type = OrderBookAdapterTypes.TITLE)
        val buyOffer = OrderBookStickyHeader(type = OrderBookAdapterTypes.BUY_HEADER)
        val sellOffer = OrderBookStickyHeader(type = OrderBookAdapterTypes.SELL_HEADER)
        val subHeader = OrderBook(type = OrderBookAdapterTypes.SUBHEADER)
        orderBooks.add(orderBooksTitle)
        orderBooks.add(buyOffer)
        orderBooks.add(subHeader)
        var id = 1
        bids.forEach {
            val item = OrderBook(id, Date(), it.price.toFloat(), it.amount.toFloat() / it.price.toFloat() , it.amount.toFloat(), OrderBookAdapterTypes.ITEM)
            orderBooks.add(item)
            id++
        }

        if (bids.isEmpty()) {
           orderBooks.add(OrderBook(type = OrderBookAdapterTypes.EMPTY))
        }

        orderBooks.add(sellOffer)
        orderBooks.add(subHeader)
        asks.forEach {
            val item = OrderBook(id, Date(), it.price.toFloat(),  it.amount.toFloat(), it.price.toFloat() * it.amount.toFloat(), OrderBookAdapterTypes.ITEM)
            orderBooks.add(item)
            id++

        }

        if (asks.isEmpty()) {
            orderBooks.add(OrderBook(type = OrderBookAdapterTypes.EMPTY))
        }

        Timber.d("loading order book complete items %s", orderBooks.size)

        Handler(Looper.getMainLooper()).post {
            initializeAdapterIfNeeded(sellingCode, buyingCode)
            orderBooksAdapter.setCurrencies(sellingCode, buyingCode)
            orderBooksAdapter.notifyDataSetChanged()
        }

        if (swipeRefresh != null) {
            swipeRefresh.isRefreshing = false
        }
    }

    override fun updateTradingCurrencies(sellingModel: SelectionModel, buyingModel: SelectionModel) {
        val selling =  AssetUtil.toDataAssetFrom(sellingModel)
        val buying = AssetUtil.toDataAssetFrom(buyingModel)

        buyingAsset = buying
        sellingAsset = selling

        MixedTypes.let(sellingAsset, buyingAsset) { sellingAsset, buyingAsset ->
            Timber.d("Updating objects in order book")
            updateList(sellingAsset.code, buyingAsset.code)
        }
    }

    private fun initializeAdapterIfNeeded(sellingCode: String, buyingCode: String) : Boolean {
        if (!::orderBooksAdapter.isInitialized && context != null) {
            orderBooksAdapter = OrderBooksAdapter(orderBooks, sellingCode, buyingCode, context as Context)
            orderBookRv.adapter = orderBooksAdapter

            val layout = StickyLayoutManager(context, orderBooksAdapter)
            orderBookRv.layoutManager = layout
            return true
        }
        return false
    }

    private fun updateList(sellingCode: String, buyingCode: String) {
        initializeAdapterIfNeeded(sellingCode, buyingCode)
        Timber.d("updateTradingCurrencies %s %s", buyingCode, sellingCode)
        orderBooksAdapter.setCurrencies(sellingCode, buyingCode)
        orderBooksAdapter.notifyDataSetChanged()

    }
}