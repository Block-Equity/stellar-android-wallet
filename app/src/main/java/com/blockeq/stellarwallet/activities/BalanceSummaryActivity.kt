package com.blockeq.stellarwallet.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.models.NativeAssetAvailability
import com.blockeq.stellarwallet.mvvm.balance.BalanceViewModel
import com.blockeq.stellarwallet.mvvm.effects.WalletViewModelPolling
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.StringFormat
import kotlinx.android.synthetic.main.activity_balance_summary.*

class BalanceSummaryActivity : BasePopupActivity() {

    private lateinit var viewModel: BalanceViewModel

    override fun setContent(): Int {
        return R.layout.activity_balance_summary
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BalanceViewModel::class.java)
        setupUI()
    }

    private fun setupUI() {
        viewModel.loadBalance().observe(this, Observer {
            if (it != null) {
                val native = it.getNativeAssetAvailability()

                totalBalanceTextView.text = native.total.toString()
                availableBalanceTextView.text = native.totalAvailable.toString()

                baseReserveAmountTextView.text = 1.toString()
                baseReserveXLMTextView.text = native.baseAmount.toString()

                trustlinesAmountTextView.text = native.trustLinesCount.toString()
                trustlinesXLMTextView.text = native.trustLinesAmount.toString()

                signersAmountTextView.text = native.additionalSignersCount.toString()
                signersXLMTextView.text = native.additionalSignersAmount.toString()

                offersAmountTextView.text = native.openOffersCount.toString()
                offersXLMTextView.text = native.openOffersAmount.toString()

                tradedAmountTextView.text = "-"
                tradedXLMTextView.text = native.postedForTradeAmount.toString()
                minimumBalanceTextView.text = native.totalAvailable.toString()
            }
        })
    }
}
