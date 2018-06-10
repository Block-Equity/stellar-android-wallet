package blockeq.com.stellarwallet.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.adapters.TradingPagerAdapter
import kotlinx.android.synthetic.main.fragment_trade.*
import kotlinx.android.synthetic.main.fragment_trade.view.*

class TradingFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_trade, container, false);

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentAdapter = TradingPagerAdapter(childFragmentManager)
        view.viewPager.adapter = fragmentAdapter
        view.tabs.setupWithViewPager(viewPager)
    }

    companion object {
        fun newInstance(): TradingFragment = TradingFragment()
    }

}