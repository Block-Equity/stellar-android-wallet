package blockeq.com.stellarwallet.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.activities.AssetsActivity
import blockeq.com.stellarwallet.activities.BalanceSummaryActivity
import blockeq.com.stellarwallet.activities.EnterAddressActivity
import blockeq.com.stellarwallet.activities.ReceiveActivity
import blockeq.com.stellarwallet.adapters.WalletRecyclerViewAdapter
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.interfaces.OnLoadAccount
import blockeq.com.stellarwallet.interfaces.OnLoadEffects
import blockeq.com.stellarwallet.models.AvailableBalance
import blockeq.com.stellarwallet.models.TotalBalance
import blockeq.com.stellarwallet.models.WalletHeterogeneousArray
import blockeq.com.stellarwallet.utils.AccountUtils
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
            val context = activity
            startActivity(Intent(context, ReceiveActivity::class.java))
            activity!!.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }

        sendButton.setOnClickListener {
            val context = activity
            startActivity(Intent(context, EnterAddressActivity::class.java))
            activity!!.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
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

            recyclerViewArrayList = WalletHeterogeneousArray(TotalBalance(AccountUtils.getTotalBalance(currAsset)),
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

            recyclerViewArrayList!!.updateTotalBalance(TotalBalance(AccountUtils.getTotalBalance(currAsset)))
            recyclerViewArrayList!!.updateEffectsList(effectsList)

            adapter!!.notifyDataSetChanged()
        }
    }

    //endregion

    //region Call backs

    override fun onLoadAccount(result: AccountResponse?) {
        recyclerViewArrayList!!.updateTotalBalance(
                TotalBalance(AccountUtils.getTotalBalance(WalletApplication.userSession.currAssetCode)))
        recyclerViewArrayList!!.updateAvailableBalance(
                AvailableBalance(WalletApplication.localStore.availableBalance!!))
    }

    override fun onError(error: ErrorResponse) {
        if (error.code == Constants.SERVER_ERROR_NOT_FOUND && walletProgressBar != null) {
            val mainHandler = Handler(context!!.mainLooper)

            mainHandler.post {
                noTransactionsTextView.visibility = View.VISIBLE
                walletProgressBar.visibility = View.GONE
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
