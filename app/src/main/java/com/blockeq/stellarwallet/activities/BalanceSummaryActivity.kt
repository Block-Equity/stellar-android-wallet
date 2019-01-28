package com.blockeq.stellarwallet.activities

import android.os.Bundle
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.StringFormat
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
        val minimumBalance = WalletApplication.userSession.getMinimumBalance()

        if (minimumBalance != null) {

            totalBalanceTextView.text = StringFormat.truncateDecimalPlaces(AccountUtils.getTotalBalance(Constants.LUMENS_ASSET_TYPE))
            availableBalanceTextView.text = StringFormat.truncateDecimalPlaces(WalletApplication.wallet.getAvailableBalance())

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
