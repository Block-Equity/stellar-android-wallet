package com.blockeq.stellarwallet.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.models.NativeAssetAvailability
import com.blockeq.stellarwallet.mvvm.balance.BalanceViewModel
import com.blockeq.stellarwallet.utils.StringFormat
import kotlinx.android.synthetic.main.activity_balance_summary.*
import java.lang.IllegalStateException

class BalanceSummaryActivity : BasePopupActivity() {

    private lateinit var viewModel: BalanceViewModel

    companion object {
        private const val ARG_EXTRA_ASSET = "ARG_EXTRA_ASSET"
        private const val ARG_EXTRA_ISSUER = "ARG_EXTRA_ISSUER"

        private const val ARG_EXTRA_IS_NATIVE = "ARG_EXTRA_IS_NATIVE"

        fun newIntent(context: Context, assetCode: String, issuer: String): Intent {
            val intent = Intent(context, BalanceSummaryActivity::class.java)
            intent.putExtra(ARG_EXTRA_ASSET, assetCode)
            intent.putExtra(ARG_EXTRA_ISSUER, issuer)
            intent.putExtra(ARG_EXTRA_IS_NATIVE, false)
            return intent
        }

        fun newNativeAssetIntent(context: Context): Intent {
            val intent = Intent(context, BalanceSummaryActivity::class.java)
            intent.putExtra(ARG_EXTRA_IS_NATIVE, true)
            return intent
        }
    }

    override fun setContent(): Int {
        return R.layout.activity_balance_summary
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BalanceViewModel::class.java)
        setupUI()
    }

    private fun setupUI() {
        val isNative = intent.getBooleanExtra(ARG_EXTRA_IS_NATIVE, false)
        val assetCode : String? = intent.getStringExtra(ARG_EXTRA_ASSET)
        val issuer : String? = intent.getStringExtra(ARG_EXTRA_ISSUER)
        if (!isNative) {
            if (assetCode == null || issuer == null) {
                throw IllegalStateException("assetCode or issuer is null")
            }
        }
        viewModel.loadBalance().observe(this, Observer {
            if (it != null) {
                if (isNative) {
                    renderNativeAsset(it.getNativeAssetAvailability())
                } else {
                        assetCode?.let{ asset -> issuer?.let { issuer ->
                            val assetAv = it.getAssetAvailability(asset, issuer)
                            totalBalanceTextView.text = StringFormat.truncateDecimalPlaces(assetAv.total.toString())
                            availableBalanceTextView.text = StringFormat.truncateDecimalPlaces(assetAv.totalAvailable.toString())
                            tradedXLMTextView.text = StringFormat.truncateDecimalPlaces(assetAv.postedForTradeAmount.toString())
                        }}

                    tradedAmountTextView.text = "-"
                    row1.visibility = View.GONE
                    row2.visibility = View.GONE
                    row3.visibility = View.GONE
                    row4.visibility = View.GONE
                    row5.visibility = View.GONE
                }
            }
        })
    }

    private fun renderNativeAsset(native : NativeAssetAvailability){
        totalBalanceTextView.text = StringFormat.truncateDecimalPlaces(native.total.toString())
        availableBalanceTextView.text = StringFormat.truncateDecimalPlaces(native.totalAvailable.toString())

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
    }
}
