package blockeq.com.stellarwallet.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import blockeq.com.stellarwallet.fragments.tabs.MyOffersTab
import blockeq.com.stellarwallet.fragments.tabs.OrderBookTab
import blockeq.com.stellarwallet.fragments.tabs.TradeTab
import blockeq.com.stellarwallet.helpers.TradingTabs
import blockeq.com.stellarwallet.helpers.TradingTabs.*

class TradingPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            Trade.ordinal -> {
                TradeTab()
            }
            OrderBook.ordinal -> OrderBookTab()
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
}