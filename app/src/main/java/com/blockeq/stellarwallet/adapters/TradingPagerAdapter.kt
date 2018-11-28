package com.blockeq.stellarwallet.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.fragments.tabs.MyOffersTabFragment
import com.blockeq.stellarwallet.fragments.tabs.OrderBookTabFragment
import com.blockeq.stellarwallet.fragments.tabs.TradeTabFragment
import com.blockeq.stellarwallet.helpers.TradingTabs
import com.blockeq.stellarwallet.helpers.TradingTabs.*
import com.blockeq.stellarwallet.models.DataAsset
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
        val dataAsset2: DataAsset
        if (balance2.assetType == "native") {
            dataAsset2 = DataAsset(balance2.assetType, "LMX", "Stellar")
        } else {
            dataAsset2 = DataAsset(balance2.assetType, balance2.assetCode, balance2.assetIssuer.accountId)
        }

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