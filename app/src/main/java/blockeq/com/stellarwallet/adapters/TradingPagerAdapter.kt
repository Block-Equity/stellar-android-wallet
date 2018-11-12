package blockeq.com.stellarwallet.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.fragments.tabs.MyOffersTabFragment
import blockeq.com.stellarwallet.fragments.tabs.OrderBookTabFragment
import blockeq.com.stellarwallet.fragments.tabs.TradeTabFragment
import blockeq.com.stellarwallet.helpers.TradingTabs
import blockeq.com.stellarwallet.helpers.TradingTabs.*
import blockeq.com.stellarwallet.models.DataAsset
import java.lang.IllegalStateException

class TradingPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return TradingTabs.values().size
    }

    override fun getItem(position: Int): Fragment {
        var accounts = WalletApplication.localStore.balances
        val balance1 = accounts!![0]
        val balance2 = accounts!![1]
        val balance3 = accounts!![2]

        val dataAsset1 = DataAsset(balance1.assetType, balance1.assetCode, balance1.assetIssuer.accountId)
        val dataAsset2 = DataAsset(balance3.assetType, "LMX", "Stellar")

        return when (position) {
            Trade.ordinal -> TradeTabFragment()

            OrderBook.ordinal -> OrderBookTabFragment.newInstance(dataAsset1, dataAsset2)
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