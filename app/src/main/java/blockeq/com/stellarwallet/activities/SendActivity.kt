package blockeq.com.stellarwallet.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.text.Html
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.interfaces.SuccessErrorCallback
import blockeq.com.stellarwallet.models.ExchangeProvider
import blockeq.com.stellarwallet.models.HorizonException
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.services.networking.Horizon
import blockeq.com.stellarwallet.utils.AccountUtils
import blockeq.com.stellarwallet.utils.NetworkUtils
import blockeq.com.stellarwallet.utils.StringFormat.Companion.getNumDecimals
import blockeq.com.stellarwallet.utils.StringFormat.Companion.hasDecimalPoint
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.davidmiguel.numberkeyboard.NumberKeyboardListener
import com.google.gson.GsonBuilder
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
    private var exchange : ExchangeProvider? = null

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

        loadExchangeProviderAddresses()
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

    private fun updateExchangeProviderText(providers : List<ExchangeProvider>) {
        val provider = providers.find { it -> it.address == address  }
        if (provider != null) {
            identifiedAddressTextView.text = Html.fromHtml(getString(R.string.send_address_identified, provider.name))
            identifiedAddressTextView.visibility = View.VISIBLE
            memoTitleTextView.text = provider.memo
            memoTextView.hint = null
        } else {
            identifiedAddressTextView.visibility = View.GONE
        }
    }

    private fun loadExchangeProviderAddresses() {
        val queue = Volley.newRequestQueue(applicationContext)

        // TODO: Use retrofit and dagger
        val request = JsonArrayRequest(Request.Method.GET, Constants.BLOCKEQ_EXCHANGES_URL, null,
                Response.Listener { response ->
                    // display response
                    val gson = GsonBuilder().create()
                    val list = gson.fromJson(response.toString(), Array<ExchangeProvider>::class.java)
                    updateExchangeProviderText(list.toList())
                },
                Response.ErrorListener {
                    it.networkResponse
                    Log.e("error", "error loading exchange providers")
                })

        queue.add(request)
    }

}
