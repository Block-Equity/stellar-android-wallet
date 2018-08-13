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

    private var orderBookTab: OrderBookTab? = null
    private var tradeTab: TradeTab? = null
    private var myOffersTab: MyOffersTab? = null

    override fun getItem(position: Int): Fragment {
        return when (position) {
            Trade.ordinal -> {
                if (tradeTab != null) {
                    tradeTab!!
                } else {
                    tradeTab = TradeTab()
                    tradeTab!!
                }
            }
            OrderBook.ordinal -> {
                if (orderBookTab != null) {
                    orderBookTab!!
                } else {
                    orderBookTab = OrderBookTab()
                    orderBookTab!!
                }
            }
            else -> {
                if (myOffersTab != null) {
                    myOffersTab!!
                } else {
                    myOffersTab = MyOffersTab()
                    myOffersTab!!
                }
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