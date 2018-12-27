package com.blockeq.stellarwallet.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.activities.AssetsActivity
import com.blockeq.stellarwallet.activities.BalanceSummaryActivity
import com.blockeq.stellarwallet.activities.EnterAddressActivity
import com.blockeq.stellarwallet.activities.ReceiveActivity
import com.blockeq.stellarwallet.adapters.WalletRecyclerViewAdapter
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.interfaces.OnLoadAccount
import com.blockeq.stellarwallet.interfaces.OnLoadEffects
import com.blockeq.stellarwallet.models.AvailableBalance
import com.blockeq.stellarwallet.models.TotalBalance
import com.blockeq.stellarwallet.models.WalletHeterogeneousArray
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.StringFormat
import kotlinx.android.synthetic.main.fragment_wallet.*
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse


class WalletFragment : BaseFragment(), OnLoadAccount, OnLoadEffects {

    private var adapter : WalletRecyclerViewAdapter? = null
    private var effectsList : java.util.ArrayList<EffectResponse>? = null
    private var recyclerViewArrayList: WalletHeterogeneousArray? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_wallet, container, false)

    companion object {
        fun newInstance(): WalletFragment = WalletFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiveButton.setOnClickListener {
            activity?.let { activityContext ->
                startActivity(Intent(activityContext, ReceiveActivity::class.java))
                activityContext.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        }

        sendButton.setOnClickListener {
            activity?.let { activityContext ->
                startActivity(EnterAddressActivity.toSend(it.context))
                activityContext.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bindAdapter()
    }

    //region User Interface


    private fun bindAdapter() {
        val currAsset = WalletApplication.userSession.currAssetCode

        if (recyclerViewArrayList == null) {
            walletProgressBar.visibility = View.VISIBLE

            recyclerViewArrayList = WalletHeterogeneousArray(TotalBalance(StringFormat.truncateDecimalPlaces(AccountUtils.getTotalBalance(currAsset))),
                    AvailableBalance(WalletApplication.localStore.availableBalance!!), Pair("Activity", "Amount"), effectsList)

            adapter = WalletRecyclerViewAdapter(activity!!, recyclerViewArrayList!!.array)
            adapter!!.setOnAssetDropdownListener(object : WalletRecyclerViewAdapter.OnAssetDropdownListener {
                override fun onAssetDropdownClicked(view: View, position: Int) {
                    val context = view.context
                    startActivity(Intent(context, AssetsActivity::class.java))
                    (context as Activity).overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
                }
            })
            adapter!!.setOnLearnMoreButtonListener(object : WalletRecyclerViewAdapter.OnLearnMoreButtonListener {
                override fun onLearnMoreButtonClicked(view: View, position: Int) {
                    val context = view.context
                    startActivity(Intent(context, BalanceSummaryActivity::class.java))
                    (context as Activity).overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
                }
            })
            walletRecyclerView.adapter = adapter
            walletRecyclerView.layoutManager = LinearLayoutManager(activity)
        } else {
            if (currAsset != Constants.LUMENS_ASSET_TYPE) {
                recyclerViewArrayList!!.hideAvailableBalance()
            } else {
                recyclerViewArrayList!!.showAvailableBalance(AvailableBalance(WalletApplication.localStore.availableBalance!!))
            }

            recyclerViewArrayList!!.updateTotalBalance(TotalBalance(StringFormat.truncateDecimalPlaces(AccountUtils.getTotalBalance(currAsset))))
            recyclerViewArrayList!!.updateEffectsList(effectsList)

            adapter!!.notifyDataSetChanged()
        }
    }

    //endregion

    //region Call backs

    override fun onLoadAccount(result: AccountResponse?) {
        recyclerViewArrayList!!.updateTotalBalance(
                TotalBalance(StringFormat.truncateDecimalPlaces(AccountUtils.getTotalBalance(WalletApplication.userSession.currAssetCode))))
        recyclerViewArrayList!!.updateAvailableBalance(
                AvailableBalance(WalletApplication.localStore.availableBalance!!))
    }

    override fun onError(error: ErrorResponse) {
        if (error.code == Constants.SERVER_ERROR_NOT_FOUND && walletProgressBar != null) {
            Handler(Looper.getMainLooper()).post {
                if (noTransactionsTextView != null) {
                    noTransactionsTextView.visibility = View.VISIBLE
                    walletProgressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onLoadEffects(result: java.util.ArrayList<EffectResponse>?) {
        if (result != null && walletProgressBar != null) {
            noTransactionsTextView.visibility = View.GONE
            walletProgressBar.visibility = View.VISIBLE

            effectsList = result
            recyclerViewArrayList!!.updateEffectsList(effectsList!!)
            adapter!!.notifyDataSetChanged()
            walletProgressBar.visibility = View.GONE
        }
    }

    //endregion

}
