package com.blockeq.stellarwallet.fragments.tabs

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.adapters.MyOffersAdapter
import com.blockeq.stellarwallet.interfaces.OnDeleteRequest
import com.blockeq.stellarwallet.models.AssetUtil
import com.blockeq.stellarwallet.models.Currency
import com.blockeq.stellarwallet.models.MyOffer
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import kotlinx.android.synthetic.main.fragment_tab_my_offers.*
import org.stellar.sdk.responses.OfferResponse
import timber.log.Timber
import java.util.*


class MyOffersTabFragment : Fragment(), OnDeleteRequest, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var appContext : Context
    private var myOffers = mutableListOf<MyOffer>()
    private var offerResponses = mutableListOf<OfferResponse>()

    private lateinit var myOffersAdapter: MyOffersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_my_offers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appContext = view.context.applicationContext
        myOffersRv.layoutManager = LinearLayoutManager(context)
        myOffersAdapter = MyOffersAdapter(myOffers, context, this)
        myOffersRv.adapter = myOffersAdapter
        val dividerItemDecoration = DividerItemDecoration(context,
                LinearLayoutManager(context).orientation)
        myOffersRv.addItemDecoration(dividerItemDecoration)
        swipeRefreshOffer.setOnRefreshListener(this)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        onRefresh()
    }

    override fun onRefresh() {
        Horizon.getOffers(object: Horizon.OnOffersListener {
            override fun onOffers(offers: ArrayList<OfferResponse>) {
                var id = 1
                offerResponses = offers
                myOffers.clear()
                offers.forEach {
                    val buyingCode : String = AssetUtil.getCode(it.buying)!!
                    val currencyBuy = Currency(1, buyingCode, "$buyingCode COIN", 0.0f, null)
                    val sellingCode : String = AssetUtil.getCode(it.selling)!!
                    val currencySelling = Currency(2, sellingCode, "$sellingCode COIN", 0.0f, null)
                    myOffers.add(MyOffer(it.id.toInt(), Date(), currencySelling, currencyBuy, it.amount.toFloat(), it.price.toFloat()))
                    id++
                }
                myOffersAdapter.notifyDataSetChanged()

                val handler = Handler()
                val runnable = Runnable {
                    if(swipeRefreshOffer != null) {
                        swipeRefreshOffer.isRefreshing = false
                    }
                }
                handler.post(runnable)
            }

            override fun onFailed(errorMessage: String) {
                Timber.e(errorMessage)
            }

        })
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

        val offer = offerResponses.find {
            it -> it.id.toInt() == offerId
        }
        val secretSeed = AccountUtils.getSecretSeed(appContext)

        if (offer != null) {
            Horizon.deleteOffer(offer.id, secretSeed, offer.selling, offer.buying, offer.price, object: Horizon.OnMarketOfferListener {
                override fun onExecuted() {
                    Toast.makeText(appContext, "Offer Deleted", Toast.LENGTH_SHORT).show()
                }

                override fun onFailed(errorMessage: String) {
                    Toast.makeText(appContext, "Failed to delete offer: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}