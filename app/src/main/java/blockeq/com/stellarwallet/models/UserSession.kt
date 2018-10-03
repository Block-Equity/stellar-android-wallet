package blockeq.com.stellarwallet.models

import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.utils.AccountUtils
import blockeq.com.stellarwallet.utils.StringFormat

class UserSession(var currAssetCode: String = Constants.LUMENS_ASSET_TYPE,
                  var currAssetName: String = Constants.LUMENS_ASSET_NAME,
                  var currAssetIssuer: String = "") {

    fun getFormattedCurrentAssetCode() : String {
        return StringFormat.formatAssetCode(currAssetCode)
    }

    fun getFormattedCurrentAvailableBalance(): String {
        val currAssetCode = getFormattedCurrentAssetCode()
        return if (currAssetCode == Constants.LUMENS_ASSET_CODE) {
            "Available: " + WalletApplication.localStore!!.availableBalance + " " +
                    currAssetCode
        } else {
            "Available: " + AccountUtils.getTotalBalance(currAssetCode) + " " +
                    getFormattedCurrentAssetCode()
        }
    }
}