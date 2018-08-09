package blockeq.com.stellarwallet.fragments.tabs

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.reusables.adapters.MyOffersAdapter
import blockeq.com.stellarwallet.reusables.models.Currency
import blockeq.com.stellarwallet.reusables.models.MyOffer
import kotlinx.android.synthetic.main.fragment_tab_my_offers.*
import java.util.*


class MyOffersTab : Fragment() {

    private var myOffers = mutableListOf<MyOffer>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_my_offers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Mockup Data
        mockupData()
        //**********

        myOffersRv.layoutManager = LinearLayoutManager(context)
        myOffersRv.adapter = MyOffersAdapter(myOffers, context)
    }

    private fun mockupData() {
        val xlm = Currency(1, "XLM", "Stellar", 38.7832f)
        val cad = Currency(2, "CAD", "Canadian Dollar", 100.00f)
        val btc = Currency(3, "BTC", "Bitcoin", 0.23243414f)
        val mobi = Currency(4, "MOBI", "Mobi", 10.4322f)
        val offer1 = MyOffer(1, Date(), mobi, xlm, 0.5623f, 0.1233f)
        val offer2 = MyOffer(2, Date(), btc, cad, 0.013432f, 112.45f)
        myOffers.add(offer1)
        myOffers.add(offer2)
    }
}