package com.blockeq.stellarwallet.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.activities.AssetsActivity
import com.blockeq.stellarwallet.activities.BalanceSummaryActivity
import com.blockeq.stellarwallet.activities.StellarAddressActivity
import com.blockeq.stellarwallet.activities.ReceiveActivity
import com.blockeq.stellarwallet.adapters.WalletRecyclerViewAdapter
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.interfaces.OnLoadAccount
import com.blockeq.stellarwallet.models.AvailableBalance
import com.blockeq.stellarwallet.models.TotalBalance
import com.blockeq.stellarwallet.models.WalletHeterogeneousArray
import com.blockeq.stellarwallet.mvvm.effects.WalletViewModel
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.StringFormat
import kotlinx.android.synthetic.main.fragment_tab_order_book.*
import kotlinx.android.synthetic.main.fragment_wallet.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse

class WalletFragment : BaseFragment(), OnLoadAccount {

    private var effectsList : java.util.ArrayList<EffectResponse>? = null
    private lateinit var recyclerViewArrayList: WalletHeterogeneousArray
    private lateinit var viewModel : WalletViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_wallet, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WalletViewModel::class.java)
        }
    }

    companion object {
        fun newInstance(): WalletFragment = WalletFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindAdapter()
        swipeRefresh_wallet.setOnRefreshListener {
            viewModel.forceRefresh()
        }

        receiveButton.setOnClickListener {
            activity?.let { activityContext ->
                startActivity(Intent(activityContext, ReceiveActivity::class.java))
                activityContext.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        }

        sendButton.setOnClickListener {
            activity?.let { activityContext ->
                startActivity(StellarAddressActivity.toSend(it.context))
                activityContext.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        }

        initViewModels()
    }

    override fun onResume() {
        super.onResume()
        updateAdapter()
    }

    //region User Interface

    private fun initViewModels() {
        viewModel.getEffects().observe(viewLifecycleOwner, Observer { it ->
            if (it != null && walletProgressBar != null) {
                noTransactionsTextView.visibility = View.GONE
                walletProgressBar.visibility = View.VISIBLE
                swipeRefresh_wallet.isRefreshing = false
                effectsList = it

                doAsync {

                    recyclerViewArrayList.updateEffectsList(effectsList!!)
                    uiThread {
                        if (walletProgressBar != null) {
                            walletRecyclerView.adapter?.notifyDataSetChanged()
                            walletProgressBar.visibility = View.GONE
                        }
                    }
                }
            }
        })

        viewModel.account.observe(viewLifecycleOwner, Observer {it ->
            if (it != null) {
                onLoadAccount(it)
            }
        })
    }



    private fun bindAdapter() {
        val currAsset = WalletApplication.userSession.currAssetCode

        if (!::recyclerViewArrayList.isInitialized) {
            walletProgressBar.visibility = View.VISIBLE

            recyclerViewArrayList = WalletHeterogeneousArray(TotalBalance(StringFormat.truncateDecimalPlaces(AccountUtils.getTotalBalance(currAsset))),
                    AvailableBalance(WalletApplication.wallet.getAvailableBalance()), Pair("Activity", "Amount"), effectsList)

            val adapter = WalletRecyclerViewAdapter(activity!!, recyclerViewArrayList.array)
            adapter.setOnAssetDropdownListener(object : WalletRecyclerViewAdapter.OnAssetDropdownListener {
                override fun onAssetDropdownClicked(view: View, position: Int) {
                    val context = view.context
                    startActivity(Intent(context, AssetsActivity::class.java))
                    (context as Activity).overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
                }
            })
            adapter.setOnLearnMoreButtonListener(object : WalletRecyclerViewAdapter.OnLearnMoreButtonListener {
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
                recyclerViewArrayList.hideAvailableBalance()
            } else {
                recyclerViewArrayList.showAvailableBalance(AvailableBalance(WalletApplication.wallet.getAvailableBalance()))
            }

            recyclerViewArrayList.updateTotalBalance(TotalBalance(StringFormat.truncateDecimalPlaces(AccountUtils.getTotalBalance(currAsset))))
            recyclerViewArrayList.updateEffectsList(effectsList)

            walletRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun updateAdapter() {
        val currAsset = WalletApplication.userSession.currAssetCode
        if (currAsset != Constants.LUMENS_ASSET_TYPE) {
            recyclerViewArrayList.hideAvailableBalance()
        } else {
            recyclerViewArrayList.showAvailableBalance(AvailableBalance(WalletApplication.wallet.getAvailableBalance()))
        }

        recyclerViewArrayList.updateTotalBalance(TotalBalance(AccountUtils.getTotalBalance(currAsset)))
        recyclerViewArrayList.updateEffectsList(effectsList)

        walletRecyclerView.adapter?.notifyDataSetChanged()
    }

    //endregion

    //region Call backs

    override fun onLoadAccount(result: AccountResponse?) {
      recyclerViewArrayList.updateTotalBalance(
                TotalBalance(StringFormat.truncateDecimalPlaces(AccountUtils.getTotalBalance(WalletApplication.userSession.currAssetCode))))
        recyclerViewArrayList.updateAvailableBalance(
                AvailableBalance(WalletApplication.wallet.getAvailableBalance()))
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

    //endregion

}
