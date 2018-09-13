package blockeq.com.stellarwallet.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.activities.MyWalletActivity
import blockeq.com.stellarwallet.activities.PinActivity
import blockeq.com.stellarwallet.activities.WalletsActivity
import blockeq.com.stellarwallet.helpers.Constants.Companion.DEFAULT_ACCOUNT_BALANCE
import blockeq.com.stellarwallet.helpers.Constants.Companion.LUMENS_ASSET_TYPE
import blockeq.com.stellarwallet.interfaces.OnLoadAccount
import kotlinx.android.synthetic.main.fragment_wallet.*
import org.stellar.sdk.responses.AccountResponse

class WalletFragment : Fragment(), OnLoadAccount {

    var handler = Handler()
    private var runnableCode : Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_wallet, container, false)

    companion object {
        fun newInstance(): WalletFragment = WalletFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        startPollingAccount()

        assetsButton.setOnClickListener {
            startActivity(Intent(activity, WalletsActivity::class.java))
            activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
        sendButton.setOnClickListener {
            startActivity(Intent(activity, MyWalletActivity::class.java))
            activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
    }

    override fun onPause() {
        super.onPause()
        endPollingAccount()
    }

    private fun setupUI() {
        loadBalance()
    }

    private fun loadBalance() {
        val balances = WalletApplication.localStore!!.balances

        if (balances != null) {
            balances.forEach {
                if (it.assetType == LUMENS_ASSET_TYPE) {
                    balanceTextView.text = it.balance
                }
            }
        } else {
            balanceTextView.text = DEFAULT_ACCOUNT_BALANCE
        }
    }


    //region API Polling

    private fun startPollingAccount() {
        runnableCode = object : Runnable {
            override fun run() {

                PinActivity.Companion.LoadAccountTask(this@WalletFragment)
                        .execute(WalletApplication.session!!.keyPair)

                handler.postDelayed(this, 5000)
            }
        }
    }

    private fun endPollingAccount() {
        handler.removeCallbacks(runnableCode)
    }

    override fun onLoadAccount(result: AccountResponse?) {
        loadBalance()
    }

    //endregion

}