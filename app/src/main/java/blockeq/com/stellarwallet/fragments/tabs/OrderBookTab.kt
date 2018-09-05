package blockeq.com.stellarwallet.fragments.tabs

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.adapters.OrderBooksAdapter
import blockeq.com.stellarwallet.models.OrderBook
import blockeq.com.stellarwallet.models.OrderBookAdapterTypes
import blockeq.com.stellarwallet.models.OrderBookStickyHeader
import com.brandongogetap.stickyheaders.StickyLayoutManager
import kotlinx.android.synthetic.main.fragment_tab_order_book.*
import java.util.*

class OrderBookTab : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var orderBooks = mutableListOf<OrderBook>()
    private lateinit var orderBooksAdapter: OrderBooksAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_order_book, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Mockup Data
        mockupData()
        //**********

        orderBooksAdapter = OrderBooksAdapter(orderBooks, "CAD", "XLM", context)
        orderBookRv.layoutManager = StickyLayoutManager(context, orderBooksAdapter)
        orderBookRv.adapter = orderBooksAdapter
        val dividerItemDecoration = DividerItemDecoration(context,
                LinearLayoutManager(context).orientation)
        orderBookRv.addItemDecoration(dividerItemDecoration)
        swipeRefresh.setOnRefreshListener(this)
    }

    override fun onRefresh() {
        //Mockup API call tor refresh
        val handler = Handler()
        val runnable = Runnable {
            swipeRefresh.isRefreshing = false
            Toast.makeText(context, getText(R.string.refreshed), Toast.LENGTH_SHORT).show()
        }
        handler.postDelayed(runnable, 1000)
        //**********
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

    fun updateTradingCurrencies(currencyCodeFrom: String?, currencyCodeTo: String?) {
        orderBooksAdapter.setCurrencies(currencyCodeFrom, currencyCodeTo)
        orderBooksAdapter.notifyDataSetChanged()
    }
}