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
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.adapters.OrderBooksAdapter
import blockeq.com.stellarwallet.models.DataAsset
import blockeq.com.stellarwallet.models.OrderBook
import blockeq.com.stellarwallet.models.OrderBookAdapterTypes
import blockeq.com.stellarwallet.models.OrderBookStickyHeader
import blockeq.com.stellarwallet.services.networking.Horizon
import com.brandongogetap.stickyheaders.StickyLayoutManager
import kotlinx.android.synthetic.main.fragment_tab_order_book.*
import org.stellar.sdk.responses.OrderBookResponse
import java.util.*

class OrderBookTabFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var orderBooks = mutableListOf<OrderBook>()
    private lateinit var orderBooksAdapter: OrderBooksAdapter
    private lateinit var buyingAsset : DataAsset
    private lateinit var sellingAsset : DataAsset

    companion object {
        private const val ARG_FROM_ASSET = "ARG_FROM_ASSET"
        private const val ARG_TO_ASSET = "ARG_TO_ASSET"

        fun newInstance(fromAsset: DataAsset, toAsset: DataAsset): Fragment {
            val args = Bundle()
            args.putParcelable(ARG_FROM_ASSET, fromAsset)
            args.putParcelable(ARG_TO_ASSET, toAsset)
            val fragment = OrderBookTabFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_order_book, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Mockup Data
//        mockupData()
        //**********

        val args = arguments
        if (args == null ||
                !args.containsKey(ARG_FROM_ASSET) || args.get(ARG_TO_ASSET) == null ||
                !args.containsKey(ARG_FROM_ASSET) || args.get(ARG_TO_ASSET) == null) {
            throw IllegalStateException("unexpected arguments, please use: ${javaClass.canonicalName}#newInstance(...)")
        }

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        buyingAsset = args.getParcelable(ARG_FROM_ASSET)
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        sellingAsset = args.getParcelable(ARG_TO_ASSET)


        orderBooksAdapter = OrderBooksAdapter(orderBooks, buyingAsset.code, sellingAsset.code, context)
        orderBookRv.layoutManager = StickyLayoutManager(context, orderBooksAdapter)
        orderBookRv.adapter = orderBooksAdapter
        val dividerItemDecoration = DividerItemDecoration(context,
                LinearLayoutManager(context).orientation)
        orderBookRv.addItemDecoration(dividerItemDecoration)
        swipeRefresh.setOnRefreshListener(this)
//        onRefresh()
    }

    override fun onRefresh() {
        if(!isAdded || isDetached || !isVisible) {
            return
        }
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

        }, buyingAsset, sellingAsset)

        if (swipeRefresh != null) {
                swipeRefresh.isRefreshing = false
                Toast.makeText(context, getText(R.string.refreshed), Toast.LENGTH_SHORT).show()
        }
        //Mockup API call tor refresh
//        val handler = Handler()
//        val runnable = Runnable {
//            if (swipeRefresh != null) {
//                swipeRefresh.isRefreshing = false
//                Toast.makeText(context, getText(R.string.refreshed), Toast.LENGTH_SHORT).show()
//            }
//        }
//        handler.postDelayed(runnable, 1000)
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
        if (::orderBooksAdapter.isInitialized) {
            orderBooksAdapter.setCurrencies(currencyCodeFrom, currencyCodeTo)
            orderBooksAdapter.notifyDataSetChanged()
        }
    }
}