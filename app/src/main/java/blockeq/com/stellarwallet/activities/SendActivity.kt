package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import blockeq.com.stellarwallet.R
import com.davidmiguel.numberkeyboard.NumberKeyboardListener
import kotlinx.android.synthetic.main.activity_base_popup.*
import kotlinx.android.synthetic.main.contents_send.*


class SendActivity : BasePopupActivity(), NumberKeyboardListener {

    private val MAX_ALLOWED_DECIMALS = 4
    val ADDRESS_DATA = "ADDRESS"


    private var amountText: String = ""
    private var amount: Double = 0.0

    var availableBalance = "6.02 XLM"

    override fun setTitle(): Int {
        return R.string.title_activity_my_wallet
    }

    override fun setContent(): Int {
        return R.layout.contents_send
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titleText.text = availableBalance

        amount_text.text = "0"
        numberKeyboard.setListener(this)

        val address = intent.getStringExtra(ADDRESS_DATA)

        findViewById<TextView>(R.id.addressEditText).apply {
            text = address
        }


        send_button.setOnClickListener { Toast.makeText(this, "send clicked", Toast.LENGTH_LONG).show() }
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

    /**
     * Shows amount in UI.
     */
    private fun showAmount(amount: String) {
        amount_text.text = if (amount.isEmpty()) "0" else amount
    }

    /**
     * Calculate the number of decimals of the string.
     */
    private fun getNumDecimals(num: String): Int {
        return if (!hasDecimalPoint(num)) {
            0
        } else num.substring(num.indexOf('.') + 1, num.length).length
    }

    /**
     * Checks whether the string has a comma.
     */
    private fun hasDecimalPoint(text: String): Boolean {
        for (i in 0 until text.length) {
            if (text[i] == '.') {
                return true
            }
        }
        return false
    }

}
