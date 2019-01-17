package com.blockeq.stellarwallet.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.adapters.TradingPagerAdapter
import com.blockeq.stellarwallet.interfaces.OnTradeCurrenciesChanged
import com.blockeq.stellarwallet.interfaces.OnUpdateOrderBook
import com.blockeq.stellarwallet.interfaces.OnUpdateTradeTab
import com.blockeq.stellarwallet.models.AssetUtil
import com.blockeq.stellarwallet.models.DataAsset
import com.blockeq.stellarwallet.models.SelectionModel
import com.blockeq.stellarwallet.remote.Horizon
import kotlinx.android.synthetic.main.fragment_trade.*
import org.stellar.sdk.responses.OrderBookResponse
import timber.log.Timber

class TradingFragment : Fragment(), OnTradeCurrenciesChanged {
    private var orderBookListener : OnUpdateOrderBook? = null
    private var tradeTabListener : OnUpdateTradeTab? = null

    companion object {
        fun newInstance(): TradingFragment = TradingFragment()
    }

    override fun onCurrencyChange(selling: SelectionModel, buying: SelectionModel) {
        val sellAsset =  AssetUtil.toDataAssetFrom(selling)
        val buyingAsset = AssetUtil.toDataAssetFrom(buying)
        if (sellAsset != null && buyingAsset != null) {
            loadOrderBook(selling.label, buying.label, sellAsset, buyingAsset)
        }
    }

    private lateinit var fragmentAdapter: TradingPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_trade, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentAdapter = TradingPagerAdapter(childFragmentManager)
        viewPager.adapter = fragmentAdapter
        viewPager.offscreenPageLimit = fragmentAdapter.count
        tabs.setupWithViewPager(viewPager)
    }

    override fun onAttachFragment(fragment: Fragment?) {
        Timber.d("onAttachFragment %s", fragment.toString())

        if (fragment is OnUpdateOrderBook) {
            orderBookListener = fragment
        }

        if (fragment is OnUpdateTradeTab) {
            tradeTabListener = fragment
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
//            loadOrderBook
        }
    }


    private fun loadOrderBook(sellingCode:String, buyingCode:String, sell : DataAsset, buy : DataAsset) {
        Timber.d("Loading order book %s %s", sellingCode, buyingCode)
        Horizon.getOrderBook(object: Horizon.OnOrderBookListener {
            override fun onOrderBook(asks: Array<OrderBookResponse.Row>, bids: Array<OrderBookResponse.Row>) {
                if (asks.isNotEmpty() && bids.isNotEmpty()) {
                    tradeTabListener?.onLastOrderBookUpdated(asks, bids)
                }

                orderBookListener?.updateOrderBook(sellingCode, buyingCode, bids, asks)
            }

            override fun onFailed(errorMessage: String) {
                Timber.d("failed to load the order book %s", errorMessage)
            }

        }, sell, buy)
    }
}