package blockeq.com.stellarwallet.fragments.tabs

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.interfaces.OnDeleteRequest
import blockeq.com.stellarwallet.reusables.adapters.MyOffersAdapter
import blockeq.com.stellarwallet.models.Currency
import blockeq.com.stellarwallet.models.MyOffer
import kotlinx.android.synthetic.main.fragment_tab_my_offers.*
import java.util.*
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.util.Log


class MyOffersTab : Fragment(), OnDeleteRequest {

    private var myOffers = mutableListOf<MyOffer>()
    private lateinit var myOffersAdapter: MyOffersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_my_offers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Mockup Data
        mockupData()
        //**********

        myOffersRv.layoutManager = LinearLayoutManager(context)
        myOffersAdapter = MyOffersAdapter(myOffers, context, this)
        myOffersRv.adapter = myOffersAdapter
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

    override fun onDialogOpen(offerId: Int) {
        if (context != null) {
            AlertDialog.Builder(context!!)
                    .setTitle(getString(R.string.deleteDialogTitle))
                    .setMessage(getString(R.string.deleteDialogText, getText(R.string.offer)))
                    .setPositiveButton(getText(R.string.yes))
                    { _, _ ->
                        deleteOffer(offerId)
                    }
                    .setNegativeButton(getText(R.string.no))
                    { _, _ ->
                    }
                    .show()
        }
    }

    private fun deleteOffer(offerId: Int) {
        val index = myOffers.indexOf(myOffers.find { offer -> offer.id == offerId })
        myOffers.removeAt(index)
        myOffersAdapter.notifyItemRemoved(index)
    }
}