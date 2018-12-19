package com.blockeq.stellarwallet.models

import android.content.Context
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.StringFormat
import java.text.DecimalFormat
import java.text.NumberFormat

class UserSession(var currAssetCode: String = Constants.LUMENS_ASSET_TYPE,
                  var currAssetName: String = Constants.LUMENS_ASSET_NAME,
                  var currAssetIssuer: String = "") {
    private val decimalFormat : NumberFormat = DecimalFormat("0.#######")

    var minimumBalance: MinimumBalance? = null
    var pin: String? = null

    fun getFormattedCurrentAssetCode() : String {
        return StringFormat.formatAssetCode(currAssetCode)
    }

    fun getFormattedCurrentAvailableBalance(context: Context): String {
        return decimalFormat.format(getAvailableBalance().toDouble()) + " " + getFormattedCurrentAssetCode()
    }

    fun getAvailableBalance(): String {
        return if (currAssetCode == Constants.LUMENS_ASSET_TYPE) {
            WalletApplication.localStore.availableBalance!!
        } else {
            AccountUtils.getTotalBalance(getFormattedCurrentAssetCode())
        }
    }
}
