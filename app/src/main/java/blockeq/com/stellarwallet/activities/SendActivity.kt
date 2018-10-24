package blockeq.com.stellarwallet.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.interfaces.SuccessErrorCallback
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.services.networking.Horizon
import blockeq.com.stellarwallet.utils.AccountUtils
import blockeq.com.stellarwallet.utils.NetworkUtils
import blockeq.com.stellarwallet.utils.StringFormat.Companion.getNumDecimals
import blockeq.com.stellarwallet.utils.StringFormat.Companion.hasDecimalPoint
import com.davidmiguel.numberkeyboard.NumberKeyboardListener
import kotlinx.android.synthetic.main.contents_send.*


class SendActivity : BasePopupActivity(), NumberKeyboardListener, SuccessErrorCallback {

    companion object {
        const val MAX_ALLOWED_DECIMALS = 4
        const val ADDRESS_DATA = "ADDRESS"
    }

    private var amountText: String = ""
    private var amount: Double = 0.0
    private var address: String = ""

    override fun setContent(): Int {
        return R.layout.contents_send
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titleText.text = WalletApplication.userSession.getFormattedCurrentAvailableBalance()
        assetCodeTextView.text = WalletApplication.userSession.getFormattedCurrentAssetCode()

        amountTextView.text = "0"
        numberKeyboard.setListener(this)

        address = intent.getStringExtra(ADDRESS_DATA)
        addressEditText.text = address

        send_button.setOnClickListener {
            if (isAmountValid()) {
                if (WalletApplication.localStore!!.showPinOnSend) {
                    launchPINView(PinType.CHECK, "", "", null, false)
                } else {
                    sendPayment()
                }
            } else {
                val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
                amountTextView.startAnimation(shakeAnimation)
            }
        }
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
        return (amount <= WalletApplication.userSession.getAvailableBalance().toDouble())
    }

    //endregion

    private fun sendPayment() {
        if (NetworkUtils(this).isNetworkAvailable()) {
            progressBar.visibility = View.VISIBLE

            val secretSeed = AccountUtils.getSecretSeed()

            Horizon.Companion.SendTask(this, address, secretSeed,
                    memoTextView.text.toString(), amountText).execute()
        } else {
            NetworkUtils(this).displayNoNetwork()
        }
    }

    //region Horizon callbacks
    override fun onSuccess() {
        progressBar.visibility = View.GONE
        Toast.makeText(this, getString(R.string.send_success_message), Toast.LENGTH_LONG).show()
        val handler = Handler()
        val runnableCode = Runnable {
            launchWallet()
        }
        handler.postDelayed(runnableCode, 1000)
    }

    override fun onError() {
        progressBar.visibility = View.GONE
        Toast.makeText(this, getString(R.string.send_error_message), Toast.LENGTH_LONG).show()
    }
    //endregion
}
