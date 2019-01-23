package com.blockeq.stellarwallet.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.activities.AssetsActivity
import com.blockeq.stellarwallet.activities.BalanceSummaryActivity
import com.blockeq.stellarwallet.activities.ReceiveActivity
import com.blockeq.stellarwallet.activities.StellarAddressActivity
import com.blockeq.stellarwallet.adapters.WalletRecyclerViewAdapter
import com.blockeq.stellarwallet.models.AvailableBalance
import com.blockeq.stellarwallet.models.TotalBalance
import com.blockeq.stellarwallet.models.WalletHeterogeneousArray
import com.blockeq.stellarwallet.mvvm.effects.WalletViewModel
import com.blockeq.stellarwallet.mvvm.effects.WalletViewState
import kotlinx.android.synthetic.main.fragment_wallet.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.stellar.sdk.responses.effects.EffectResponse
import timber.log.Timber

class WalletFragment : BaseFragment() {

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

        createAdapter()
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
        //TODO review this
//        updateAdapter()
    }

    //region User Interface

    private fun initViewModels() {
        viewModel.walletViewState().observe(this, Observer {
            it?.let { that ->
                Timber.d("observed = ${it.status}")
                when(that.status) {
                    WalletViewState.WalletStatus.UNKNOWN  -> { }
                    WalletViewState.WalletStatus.UNFUNDED  -> { }
                    WalletViewState.WalletStatus.ACTIVE  -> {
                        noTransactionsTextView.visibility = View.GONE
                        walletProgressBar.visibility = View.VISIBLE
                        doAsync {
                            updateListData(that.effectList!!, that.activeAssetCode, that.availableBalance, that.totalBalance)
                            uiThread {
                                if (walletProgressBar != null) {
                                    swipeRefresh_wallet.isRefreshing = false
                                    walletRecyclerView.adapter?.notifyDataSetChanged()
                                    walletProgressBar.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }
        })

    }

    private fun createAdapter() {
        walletProgressBar.visibility = View.VISIBLE

        recyclerViewArrayList = WalletHeterogeneousArray(TotalBalance("Lumens", "XLM","-1"),
                AvailableBalance("XLM", "-1"), Pair("Activity", "Amount"), arrayListOf())

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
    }

    private fun updateListData(list : ArrayList<EffectResponse>, activeAsset : String, availableBalance: AvailableBalance, totalAssetBalance : TotalBalance){
        recyclerViewArrayList.showAvailableBalance(availableBalance)
        recyclerViewArrayList.updateTotalBalance(totalAssetBalance)
        recyclerViewArrayList.updateEffectsList(activeAsset, list)
        recyclerViewArrayList.updateAvailableBalance(availableBalance)

    }

    //endregion

    //region Call backs
//TODO: propagate errors to ui?
//    override fun onError(error: ErrorResponse) {
//        if (error.code == Constants.SERVER_ERROR_NOT_FOUND && walletProgressBar != null) {
//            Handler(Looper.getMainLooper()).post {
//                if (noTransactionsTextView != null) {
//                    noTransactionsTextView.visibility = View.VISIBLE
//                    walletProgressBar.visibility = View.GONE
//                }
//            }
//        }
//    }

    //endregion

}
