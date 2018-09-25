package blockeq.com.stellarwallet.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.activities.ReceiveActivity
import blockeq.com.stellarwallet.activities.EnterAddressActivity
import blockeq.com.stellarwallet.activities.WalletsActivity
import blockeq.com.stellarwallet.adapters.WalletRecyclerViewAdapter
import blockeq.com.stellarwallet.helpers.Constants.Companion.LUMENS_ASSET_TYPE
import blockeq.com.stellarwallet.interfaces.OnLoadAccount
import blockeq.com.stellarwallet.interfaces.OnLoadEffects
import blockeq.com.stellarwallet.models.AvailableBalance
import blockeq.com.stellarwallet.models.TotalBalance
import blockeq.com.stellarwallet.models.WalletHeterogenousArray
import blockeq.com.stellarwallet.services.networking.Horizon
import blockeq.com.stellarwallet.services.networking.Horizon.Companion.getBalance
import blockeq.com.stellarwallet.utils.NetworkUtils
import kotlinx.android.synthetic.main.fragment_wallet.*
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse


class WalletFragment : BaseFragment(), OnLoadAccount, OnLoadEffects {

    private var handler = Handler()
    private var runnableCode : Runnable? = null
    private var adapter : WalletRecyclerViewAdapter? = null
    private var effectsList : java.util.ArrayList<EffectResponse>? = null
    private var recyclerViewArrayList: WalletHeterogenousArray? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_wallet, container, false)

    companion object {
        fun newInstance(): WalletFragment = WalletFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPollingAccount()
        setupUI()

        receiveButton.setOnClickListener {
            startActivity(Intent(activity, ReceiveActivity::class.java))
            activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }

        sendButton.setOnClickListener {
            startActivity(Intent(activity, EnterAddressActivity::class.java))
            activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
    }

    override fun onPause() {
        super.onPause()
        endPollingAccount()
    }

    //region User Interface

    private fun setupUI() {
        bindAdapter()
    }

    private fun bindAdapter() {
        recyclerViewArrayList = WalletHeterogenousArray(TotalBalance(getBalance()),
                AvailableBalance(getBalance()), Pair("Activity", "Amount"), effectsList)

        adapter = WalletRecyclerViewAdapter(activity!!, recyclerViewArrayList!!.array)
        adapter!!.setOnAssetDropdownListener(object : WalletRecyclerViewAdapter.OnAssetDropdownListener {
            override fun onAssetDropdownClicked(view: View, position: Int) {
                startActivity(Intent(activity, WalletsActivity::class.java))
                activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }

        })
        adapter!!.setOnLearnMoreButtonListener(object : WalletRecyclerViewAdapter.OnLearnMoreButtonListener {
            override fun onLearnMoreButtonClicked(view: View, position: Int) {

            }

        })
        walletRecyclerView.adapter = adapter
        walletRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

    //endregion

    //region Call backs

    override fun onLoadAccount(result: AccountResponse?) {
        if (result != null) {

            result.balances?.forEach {
                if (it.assetType == LUMENS_ASSET_TYPE) {
                    recyclerViewArrayList!!.updateTotalBalance(TotalBalance(it.balance))
                }
            }

            WalletApplication.localStore!!.balances = result.balances
        }
    }

    override fun onLoadEffects(result: java.util.ArrayList<EffectResponse>?) {
        if (result != null) {
            recyclerViewArrayList!!.updateEffectsList(result)
            adapter!!.notifyDataSetChanged()
        }
    }

    //endregion


    //region API Polling

    //TODO polling for only non-created accounts on Stellar.
    private fun startPollingAccount() {
        runnableCode = object : Runnable {
            override fun run() {

                if (WalletApplication.session != null && NetworkUtils(activity!!).isNetworkAvailable()) {

                    Horizon.Companion.LoadAccountTask(this@WalletFragment)
                            .execute(WalletApplication.session!!.keyPair)

                    Horizon.Companion.LoadEffectsTask(this@WalletFragment)
                            .execute(WalletApplication.session!!.keyPair)
                } else {
                    NetworkUtils(activity!!).displayNoNetwork()
                }

                handler.postDelayed(this, 5000)
            }
        }

        handler.post(runnableCode)
    }

    private fun endPollingAccount() {
        handler.removeCallbacks(runnableCode)
    }

    //endregion

}
