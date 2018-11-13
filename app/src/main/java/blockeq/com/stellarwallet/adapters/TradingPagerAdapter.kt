package blockeq.com.stellarwallet.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import blockeq.com.stellarwallet.fragments.tabs.MyOffersTabFragment
import blockeq.com.stellarwallet.fragments.tabs.OrderBookTabFragment
import blockeq.com.stellarwallet.fragments.tabs.TradeTabFragment
import blockeq.com.stellarwallet.helpers.TradingTabs
import blockeq.com.stellarwallet.helpers.TradingTabs.*
import java.lang.IllegalStateException

class TradingPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return TradingTabs.values().size
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            Trade.ordinal -> TradeTabFragment()
            OrderBook.ordinal -> OrderBookTabFragment()
            MyOffers.ordinal -> MyOffersTabFragment()
            else -> throw IllegalStateException("position not valid for" + TradingPagerAdapter::class.simpleName)
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            Trade.ordinal -> Trade.title
            OrderBook.ordinal -> OrderBook.title
            MyOffers.ordinal -> MyOffers.title
            else -> throw IllegalStateException("position not valid for" + TradingPagerAdapter::class.simpleName)
        }
    }
}