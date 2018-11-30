package com.blockeq.stellarwallet.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.adapters.TradingPagerAdapter
import com.blockeq.stellarwallet.fragments.tabs.OrderBookTabFragment
import com.blockeq.stellarwallet.interfaces.OnTradeCurrenciesChanged
import com.blockeq.stellarwallet.interfaces.OnUpdateTradingCurrencies
import com.blockeq.stellarwallet.models.SelectionModel
import kotlinx.android.synthetic.main.fragment_trade.*
import timber.log.Timber

class TradingFragment : Fragment(), OnTradeCurrenciesChanged {
    private var assetFrom: SelectionModel? = null
    private var assetTo: SelectionModel? = null
    private var listener : OnUpdateTradingCurrencies? = null
    companion object {
        fun newInstance(): TradingFragment = TradingFragment()
    }

    override fun onCurrencyChange(currencyCodeFrom: SelectionModel, currencyCodeTo: SelectionModel) {
        assetFrom = currencyCodeFrom
        assetTo = currencyCodeTo
        if (listener != null) {
           listener!!.updateTradingCurrencies(assetFrom!!, assetTo!!)
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

        if (fragment is OrderBookTabFragment) {
            listener = fragment
        }
    }

}