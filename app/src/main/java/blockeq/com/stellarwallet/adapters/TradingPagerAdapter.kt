package blockeq.com.stellarwallet.adapters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import blockeq.com.stellarwallet.fragments.tabs.MyOffersTab
import blockeq.com.stellarwallet.fragments.tabs.OrderBookTab
import blockeq.com.stellarwallet.fragments.tabs.TradeTab
import blockeq.com.stellarwallet.helpers.TradingTabs
import blockeq.com.stellarwallet.helpers.TradingTabs.*
import blockeq.com.stellarwallet.interfaces.OnTradeCurrenciesChange

class TradingPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm), OnTradeCurrenciesChange {

    private lateinit var orderBookTab: OrderBookTab
    private lateinit var tradeTab: TradeTab

    companion object {
        val CURRNECY_CHANGE_KEY = "Currency Change Key"
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            Trade.ordinal -> {
                tradeTab = TradeTab()
                val bundle: Bundle = Bundle()
                bundle.putSerializable(CURRNECY_CHANGE_KEY, this)
                tradeTab.arguments = bundle
                tradeTab
            }
            OrderBook.ordinal -> {
                orderBookTab = OrderBookTab()
                orderBookTab
            }
            else -> {
                return MyOffersTab()
            }
        }
    }

    override fun getCount(): Int {
        return TradingTabs.values().size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            Trade.ordinal -> Trade.title
            OrderBook.ordinal -> OrderBook.title
            else -> {
                return MyOffers.title
            }
        }
    }

    override fun onCurrencyChange(currencyCodeFrom: String?, currencyCodeTo: String?) {
        orderBookTab.updateTradingCurrencies(currencyCodeFrom, currencyCodeTo)
    }
}