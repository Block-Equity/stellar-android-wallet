package com.blockeq.stellarwallet.fragments.tabs

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.Snackbar
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
import com.blockeq.stellarwallet.adapters.MyOffersAdapter
import com.blockeq.stellarwallet.interfaces.OnDeleteRequest
import com.blockeq.stellarwallet.models.AssetUtil
import com.blockeq.stellarwallet.models.Currency
import com.blockeq.stellarwallet.models.MyOffer
import com.blockeq.stellarwallet.mvvm.account.AccountRepository
import com.blockeq.stellarwallet.mvvm.trading.OffersRepository
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import kotlinx.android.synthetic.main.fragment_tab_my_offers.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.stellar.sdk.responses.AccountResponse
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
        myOffersRv.layoutManager = LinearLayoutManager(appContext)
        myOffersAdapter = MyOffersAdapter(myOffers, view.context, this)
        myOffersRv.adapter = myOffersAdapter
        val dividerItemDecoration = DividerItemDecoration(context,
                LinearLayoutManager(context).orientation)
        myOffersRv.addItemDecoration(dividerItemDecoration)
        swipeRefreshOffer.setOnRefreshListener(this)

        OffersRepository.loadOffers().observe(this, android.arch.lifecycle.Observer {
            if (it!= null) {
                if (it.size == 0) {
                    empty_view.visibility = View.VISIBLE
                } else {
                    empty_view.visibility = View.GONE
                }
                var id = 1
                offerResponses = it
                myOffers.clear()
                it.forEach { that ->
                    val buyingCode : String = AssetUtil.getCode(that.buying)!!
                    val currencyBuy = Currency(1, buyingCode, "$buyingCode COIN", 0.0, null)
                    val sellingCode : String = AssetUtil.getCode(that.selling)!!
                    val currencySelling = Currency(2, sellingCode, "$sellingCode COIN", 0.0, null)
                    myOffers.add(MyOffer(that.id.toInt(), Date(), currencySelling, currencyBuy, that.amount.toFloat(), that.amount.toFloat() * that.price.toFloat()))
                    id++
                }
                myOffersAdapter.notifyDataSetChanged()

                setRefreshingFalse()
            } else {
                // error
                setRefreshingFalse()
            }

        })

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
//        onRefresh()
    }

    override fun onRefresh() {
        Timber.d("refreshing offers")
        OffersRepository.refresh()
    }

    private fun setRefreshingFalse(){
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            if (swipeRefreshOffer != null) {
                swipeRefreshOffer.isRefreshing = false
            }
        }
        handler.post(runnable)
    }

    override fun onDialogOpen(offerId: Int) {
        context?.let {
            AlertDialog.Builder(it)
                    .setTitle(getString(R.string.deleteDialogTitle))
                    .setMessage(getString(R.string.deleteDialogText, getText(R.string.offer)))
                    .setPositiveButton(getText(R.string.yes)) { _, _ -> deleteOffer(offerId)}
                    .setNegativeButton(getText(R.string.no)) { dialog, _ -> dialog.dismiss()}
                    .show()
        }
    }

    private fun deleteOffer(offerId: Int) {
        val index = myOffers.indexOf(myOffers.find { offer -> offer.id == offerId })
        if (index == -1) {
            Timber.e("failed to delete, the offer does not exist in the array")
            return
        }
        myOffers.removeAt(index)
        myOffersAdapter.notifyItemRemoved(index)

        if (myOffers.size == 0) {
            empty_view.visibility = View.VISIBLE
        }

        val offer = offerResponses.find {
            it.id.toInt() == offerId
        }

        if (offer != null) {
            val secretSeed = AccountUtils.getSecretSeed(appContext)
            Horizon.deleteOffer(offer.id, secretSeed, offer.selling, offer.buying, offer.price, object: Horizon.OnMarketOfferListener {
                override fun onExecuted() {
                    runOnUiThread {
                        activity?.let {
                            if (!it.isFinishing) {
                                Toast.makeText(appContext, "Order has been deleted", Toast.LENGTH_SHORT).show()
                            }
                            AccountRepository.refresh()
                        }
                    }

                }

                override fun onFailed(errorMessage: String) {
                    runOnUiThread {
                        activity?.let {
                            if (!it.isFinishing) {
                                Toast.makeText(appContext, "Failed to delete offer: $errorMessage", Toast.LENGTH_SHORT).show()

                                val snackbar = Snackbar.make(it.findViewById(R.id.content_container),
                                        "failed to delete the offer", Snackbar.LENGTH_INDEFINITE)
                                snackbar.setAction("retry") {
                                    deleteOffer(offerId)
                                }
                                snackbar.show()
                            }
                        }
                    }
                }
            })
        }
    }
}