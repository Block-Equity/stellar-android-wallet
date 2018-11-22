package com.blockeq.stellarwallet.models

import android.content.Context
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.StringFormat

class UserSession(var currAssetCode: String = Constants.LUMENS_ASSET_TYPE,
                  var currAssetName: String = Constants.LUMENS_ASSET_NAME,
                  var currAssetIssuer: String = "") {

    var minimumBalance: MinimumBalance? = null
    var pin: String? = null

    fun getFormattedCurrentAssetCode() : String {
        return StringFormat.formatAssetCode(currAssetCode)
    }

    fun getFormattedCurrentAvailableBalance(context: Context): String {
        return String.format(context.getString(R.string.available_balance_template),
                getAvailableBalance(), getFormattedCurrentAssetCode())
    }

    fun getAvailableBalance(): String {
        return if (currAssetCode == Constants.LUMENS_ASSET_TYPE) {
            WalletApplication.localStore.availableBalance!!
        } else {
            AccountUtils.getTotalBalance(getFormattedCurrentAssetCode())
        }
    }
}
