package com.blockeq.stellarwallet.activities

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.interfaces.SuccessErrorCallback
import com.blockeq.stellarwallet.models.ExchangeApiModel
import com.blockeq.stellarwallet.models.HorizonException
import com.blockeq.stellarwallet.models.PinType
import com.blockeq.stellarwallet.networking.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.NetworkUtils
import com.blockeq.stellarwallet.utils.StringFormat.Companion.getNumDecimals
import com.blockeq.stellarwallet.utils.StringFormat.Companion.hasDecimalPoint
import com.blockeq.stellarwallet.vmodels.ExchangeEntity
import com.blockeq.stellarwallet.vmodels.ExchangeViewModel
import com.davidmiguel.numberkeyboard.NumberKeyboardListener
import kotlinx.android.synthetic.main.contents_send.*

class SendActivity : BasePopupActivity(), NumberKeyboardListener, SuccessErrorCallback {

    companion object {
        private const val MAX_ALLOWED_DECIMALS = 4
        private const val ARG_ADDRESS_DATA = "ARG_ADDRESS_DATA"

        fun newIntent(context: Context, address: String): Intent {
            val intent = Intent(context, SendActivity::class.java)
            intent.putExtra(ARG_ADDRESS_DATA, address)
            return intent
        }
    }

    private var amountText: String = ""
    private var amount: Double = 0.0
    private var address: String = ""
    private var exchange : ExchangeApiModel? = null

    override fun setContent(): Int {
        return R.layout.contents_send
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titleText.text = WalletApplication.userSession.getFormattedCurrentAvailableBalance(applicationContext)
        assetCodeTextView.text = WalletApplication.userSession.getFormattedCurrentAssetCode()

        amountTextView.text = "0"
        numberKeyboard.setListener(this)

        if (intent.hasExtra(ARG_ADDRESS_DATA)) {
            address = intent.getStringExtra(ARG_ADDRESS_DATA)
        } else {
            throw IllegalStateException("failed to parse the arguments, please use ${SendActivity::class.java.simpleName}#newIntent(...)")
        }

        addressEditText.text = address

        send_button.setOnClickListener {
            if (isAmountValid()) {
                if (WalletApplication.localStore.showPinOnSend) {
                    launchPINView(PinType.CHECK, "", "", null, false)
                } else {
                    sendPayment()
                }
            } else {
                val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
                amountTextView.startAnimation(shakeAnimation)
            }
        }

        val viewModel = ViewModelProviders.of(this).get(ExchangeViewModel::class.java)
        viewModel.exchangeMatching(address).observe(this, Observer<ExchangeEntity> {
           updateExchangeProviderText(it)
        })
    }

    //region User Interface

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinActivity.PIN_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    sendPayment()
                }
                Activity.RESULT_CANCELED -> {}
                else -> finish()
            }
        }
    }

    override fun onNumberClicked(number: Int) {
        if (amountText.isEmpty() && number == 0) {
            return
        }
        updateAmount(amountText + number)
    }

    override fun onLeftAuxButtonClicked() {
        if (!hasDecimalPoint(amountText)) {
            amountText = if (amountText.isEmpty()) "0." else "$amountText."
            showAmount(amountText)
        }
    }

    override fun onRightAuxButtonClicked() {
        if (amountText.isEmpty()) {
            return
        }
        var newAmountText: String
        if (amountText.length <= 1) {
            newAmountText = ""
        } else {
            newAmountText = amountText.substring(0, amountText.length - 1)
            if (newAmountText[newAmountText.length - 1] == '.') {
                newAmountText = newAmountText.substring(0, newAmountText.length - 1)
            }
            if ("0" == newAmountText) {
                newAmountText = ""
            }
        }
        updateAmount(newAmountText)
    }

    private fun updateAmount(newAmountText: String) {
        val newAmount = if (newAmountText.isEmpty()) 0.0 else java.lang.Double.parseDouble(newAmountText)
        if (newAmount >= 0.0 && getNumDecimals(newAmountText) <= MAX_ALLOWED_DECIMALS) {
            amountText = newAmountText
            amount = newAmount
            showAmount(amountText)
        }
    }

    private fun showAmount(amount: String) {
        amountTextView.text = if (amount.isEmpty()) "0" else amount
        if (isAmountValid()) {
            amountTextView.setTextColor(ContextCompat.getColor(this, R.color.toryBlue))
        } else {
            amountTextView.setTextColor(ContextCompat.getColor(this, R.color.apricot))
        }
    }

    private fun isAmountValid() : Boolean {
        return amount <= WalletApplication.userSession.getAvailableBalance().toDouble() && amount != 0.0
    }

    //endregion

    private fun sendPayment() {
        exchange.let {
            if (it != null && memoTextView.text.isEmpty()) {
                Toast.makeText(applicationContext, "you must write a {${it.memo}}", Toast.LENGTH_SHORT).show()
            } else {
                if (NetworkUtils(applicationContext).isNetworkAvailable()) {
                    progressBar.visibility = View.VISIBLE

                    val secretSeed = AccountUtils.getSecretSeed(applicationContext)

                    Horizon.getSendTask(this, address, secretSeed,
                            memoTextView.text.toString(), amountText).execute()
                } else {
                    NetworkUtils(applicationContext).displayNoNetwork()
                }
            }
        }
    }

    //region Horizon callbacks
    override fun onSuccess() {
        progressBar.visibility = View.GONE
        Toast.makeText(applicationContext, getString(R.string.send_success_message), Toast.LENGTH_LONG).show()
        val handler = Handler()
        val runnableCode = Runnable {
            launchWallet()
        }
        handler.postDelayed(runnableCode, 1000)
    }

    override fun onError(error: HorizonException) {
        progressBar.visibility = View.GONE
        Toast.makeText(this, error.message(this), Toast.LENGTH_LONG).show()
    }
    //endregion

    @Suppress("DEPRECATION")
    private fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

    private fun updateExchangeProviderText(provider : ExchangeEntity?) {
        if (provider != null) {
            identifiedAddressTextView.text = fromHtml(getString(R.string.send_address_identified, provider.name))
            identifiedAddressTextView.visibility = View.VISIBLE
            memoTitleTextView.text = provider.memo
            memoTextView.hint = null
        } else {
            identifiedAddressTextView.visibility = View.GONE
        }
    }

}
