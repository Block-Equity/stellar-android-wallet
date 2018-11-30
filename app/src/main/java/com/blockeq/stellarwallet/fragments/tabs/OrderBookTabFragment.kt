package com.blockeq.stellarwallet.fragments.tabs

import android.os.Bundle
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
import java.util.*
import com.brandongogetap.stickyheaders.StickyLayoutManager
import timber.log.Timber

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
        //Mockup Data
//        mockupData()
        //**********


        val dividerItemDecoration = DividerItemDecoration(context,
                LinearLayoutManager(context).orientation)
        orderBookRv.addItemDecoration(dividerItemDecoration)
        swipeRefresh.setOnRefreshListener(this)

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && orderBookRv != null && buyingAsset != null && sellingAsset != null) {
            updateList(buyingAsset!!.code, sellingAsset!!.code)
            onRefresh()

        }
    }

    override fun onRefresh() {
        if (!isAdded || isDetached || !isVisible) {
            return
        }

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
                orderBooksAdapter.notifyDataSetChanged()
            }

            override fun onFailed(errorMessage: String) {

            }

        }, buy, sell)

        if (swipeRefresh != null) {
            swipeRefresh.isRefreshing = false
            Toast.makeText(context, getText(R.string.refreshed), Toast.LENGTH_SHORT).show()
        }
    }
    private fun mockupData() {
        orderBooks.clear()
        val orderBooksTitle = OrderBook(type = OrderBookAdapterTypes.TITLE)
        val buyOffer = OrderBookStickyHeader(type = OrderBookAdapterTypes.BUY_HEADER)
        val sellOffer = OrderBookStickyHeader(type = OrderBookAdapterTypes.SELL_HEADER)
        val subheader = OrderBook(type = OrderBookAdapterTypes.SUBHEADER)
        val item1 = OrderBook(1, Date(), 0.3000f, 29.5001f, 8.8500f, OrderBookAdapterTypes.ITEM)
        val item2 = OrderBook(2, Date(), 0.2857f, 70.0000f, 20.0000f, OrderBookAdapterTypes.ITEM)
        val item3 = OrderBook(3, Date(), 0.2500f, 60.0000f, 15.0000f, OrderBookAdapterTypes.ITEM)
        val item4 = OrderBook(4, Date(), 0.1875f, 40.0000f, 7.5000f, OrderBookAdapterTypes.ITEM)
        val item5 = OrderBook(5, Date(), 0.0600f, 500.0000f, 30.0000f, OrderBookAdapterTypes.ITEM)
        val item6 = OrderBook(6, Date(), 1.0000f, 20.0000f, 20.0000f, OrderBookAdapterTypes.ITEM)
        val item7 = OrderBook(7, Date(), 1.8000f, 1.0000f, 1.8000f, OrderBookAdapterTypes.ITEM)
        val item8 = OrderBook(8, Date(), 2.6000f, 1.0000f, 2.6000f, OrderBookAdapterTypes.ITEM)
        val item9 = OrderBook(9, Date(), 3.8000f, 1.0000f, 3.8000f, OrderBookAdapterTypes.ITEM)
        orderBooks.add(orderBooksTitle)
        orderBooks.add(buyOffer)
        orderBooks.add(subheader)
        orderBooks.add(item1)
        orderBooks.add(item2)
        orderBooks.add(item3)
        orderBooks.add(item4)
        orderBooks.add(item5)
        orderBooks.add(sellOffer)
        orderBooks.add(subheader)
        orderBooks.add(item6)
        orderBooks.add(item7)
        orderBooks.add(item8)
        orderBooks.add(item9)
        orderBooks.add(item9)
        orderBooks.add(item9)
        orderBooks.add(item9)
        orderBooks.add(item9)
        orderBooks.add(item9)
        orderBooks.add(item9)
        orderBooks.add(item9)
        orderBooks.add(item9)
        orderBooks.add(item9)
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

    private fun updateList(codeFrom: String, codeTo: String){
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