package blockeq.com.stellarwallet.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import blockeq.com.stellarwallet.fragments.tabs.MyOffersTab
import blockeq.com.stellarwallet.fragments.tabs.OrderBookTab
import blockeq.com.stellarwallet.fragments.tabs.TradeTab

class TradingPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                TradeTab()
            }
            1 -> OrderBookTab()
            else -> {
                return MyOffersTab()
            }
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Trade"
            1 -> "Order Book"
            else -> {
                return "My Offers"
            }
        }
    }
}