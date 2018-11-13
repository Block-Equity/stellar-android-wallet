package blockeq.com.stellarwallet.activities

import android.os.Bundle
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.utils.AccountUtils
import kotlinx.android.synthetic.main.activity_balance_summary.*

class BalanceSummaryActivity : BasePopupActivity() {

    val DEFAULT_BALANCE = "0"
    val BASE_RESERVE_AMOUNT = "1"

    override fun setContent(): Int {
        return R.layout.activity_balance_summary
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        val minimumBalance = WalletApplication.userSession.minimumBalance

        if (minimumBalance != null) {

            totalBalanceTextView.text = AccountUtils.getTotalBalance(Constants.LUMENS_ASSET_TYPE)
            availableBalanceTextView.text = WalletApplication.localStore.availableBalance

            baseReserveAmountTextView.text = BASE_RESERVE_AMOUNT
            baseReserveXLMTextView.text = Constants.BASE_RESERVE.toString()

            trustlinesAmountTextView.text = minimumBalance.trustlines.count.toString()
            trustlinesXLMTextView.text = minimumBalance.trustlines.amount.toString()

            offersAmountTextView.text = minimumBalance.offers.count.toString()
            offersXLMTextView.text = minimumBalance.offers.amount.toString()

            signersAmountTextView.text = minimumBalance.signers.count.toString()
            signersXLMTextView.text = minimumBalance.signers.amount.toString()

            minimumBalanceTextView.text = minimumBalance.totalAmount.toString()
        } else {

            totalBalanceTextView.text = Constants.DEFAULT_ACCOUNT_BALANCE
            availableBalanceTextView.text = Constants.DEFAULT_ACCOUNT_BALANCE

            baseReserveAmountTextView.text = DEFAULT_BALANCE
            baseReserveXLMTextView.text = DEFAULT_BALANCE

            trustlinesAmountTextView.text = DEFAULT_BALANCE
            trustlinesXLMTextView.text = DEFAULT_BALANCE

            offersAmountTextView.text = DEFAULT_BALANCE
            offersXLMTextView.text = DEFAULT_BALANCE

            signersAmountTextView.text = DEFAULT_BALANCE
            signersXLMTextView.text = DEFAULT_BALANCE

            minimumBalanceTextView.text = Constants.DEFAULT_ACCOUNT_BALANCE
        }
    }
}
