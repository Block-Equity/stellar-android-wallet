package blockeq.com.stellarwallet.models

import android.content.Context
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.utils.AccountUtils
import blockeq.com.stellarwallet.utils.StringFormat

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
