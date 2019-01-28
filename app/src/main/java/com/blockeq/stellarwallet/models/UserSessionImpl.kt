package com.blockeq.stellarwallet.models

import android.content.Context
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.StringFormat
import java.text.DecimalFormat

class UserSessionImpl : UserSession {

    override fun setMinimumBalance(minimumBalance: MinimumBalance) {
        minimumBalanceSession = minimumBalance
    }

    override fun getMinimumBalance(): MinimumBalance? {
       return minimumBalanceSession
    }

    private var asset : SessionAsset = DefaultAsset()

    override fun getSessionAsset(): SessionAsset {
      return asset
    }

    override fun setSessionAsset(sessionAsset: SessionAsset){
        asset = sessionAsset
    }

    override fun setPin(pin:String?) {
      sessionPin = pin
    }

    private val decimalFormat : DecimalFormat = DecimalFormat("0.#######")

    private var minimumBalanceSession: MinimumBalance? = null
    private var sessionPin: String? = null

    override fun getPin(): String? {
        return sessionPin
    }
    override fun getFormattedCurrentAssetCode() : String {
        return StringFormat.formatAssetCode(getSessionAsset().assetCode)
    }

    @Suppress("UNUSED_PARAMETER")
    override fun getFormattedCurrentAvailableBalance(context: Context): String {
        return decimalFormat.format(getAvailableBalance().toDouble()) + " " + getFormattedCurrentAssetCode()
    }

    override fun getAvailableBalance(): String {
        return if (getSessionAsset().assetCode == Constants.LUMENS_ASSET_TYPE) {
            WalletApplication.wallet.getAvailableBalance()
        } else {
            AccountUtils.getTotalBalance(getFormattedCurrentAssetCode())
        }
    }
}
