package blockeq.com.stellarwallet.models

import android.content.res.Resources
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.utils.AccountUtils
import blockeq.com.stellarwallet.utils.StringFormat

class UserSession(var currAssetCode: String = Constants.LUMENS_ASSET_TYPE,
                  var currAssetName: String = Constants.LUMENS_ASSET_NAME,
                  var currAssetIssuer: String = "") {

    var minimumBalance: MinimumBalance? = null

    fun getFormattedCurrentAssetCode() : String {
        return StringFormat.formatAssetCode(currAssetCode)
    }

    fun getFormattedCurrentAvailableBalance(): String {
        val currAssetCode = getFormattedCurrentAssetCode()
        return if (currAssetCode == Constants.LUMENS_ASSET_CODE) {
            String.format(WalletApplication.applicationContext().getString(R.string.available_balance_template),
                    WalletApplication.localStore!!.availableBalance, currAssetCode)
        } else {
            String.format(WalletApplication.applicationContext().getString(R.string.available_balance_template),
                    AccountUtils.getTotalBalance(currAssetCode), getFormattedCurrentAssetCode())
        }
    }
}