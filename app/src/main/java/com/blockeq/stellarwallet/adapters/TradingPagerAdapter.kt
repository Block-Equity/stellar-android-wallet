package com.blockeq.stellarwallet.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.blockeq.stellarwallet.fragments.tabs.MyOffersTabFragment
import com.blockeq.stellarwallet.fragments.tabs.OrderBookTabFragment
import com.blockeq.stellarwallet.fragments.tabs.TradeTabFragment
import com.blockeq.stellarwallet.helpers.TradingTabs
import com.blockeq.stellarwallet.helpers.TradingTabs.*

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