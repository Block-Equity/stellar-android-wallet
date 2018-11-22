package com.blockeq.stellarwallet.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.adapters.TradingPagerAdapter
import com.blockeq.stellarwallet.fragments.tabs.OrderBookTabFragment
import com.blockeq.stellarwallet.helpers.TradingTabs
import com.blockeq.stellarwallet.interfaces.OnTradeCurrenciesChange
import kotlinx.android.synthetic.main.fragment_trade.*

class TradingFragment : Fragment(), OnTradeCurrenciesChange {
    private lateinit var fragmentAdapter: TradingPagerAdapter

    override fun onCurrencyChange(currencyCodeFrom: String?, currencyCodeTo: String?) {
        (fragmentAdapter.getItem(TradingTabs.OrderBook.ordinal) as OrderBookTabFragment).updateTradingCurrencies(currencyCodeFrom, currencyCodeTo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_trade, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentAdapter = TradingPagerAdapter(childFragmentManager)
        viewPager.adapter = fragmentAdapter
        viewPager.offscreenPageLimit = fragmentAdapter.count
        tabs.setupWithViewPager(viewPager)
    }

    companion object {
        fun newInstance(): TradingFragment = TradingFragment()
    }

}