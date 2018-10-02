package blockeq.com.stellarwallet.models

import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.utils.AccountUtils

class UserSession(var currAssetCode: String = Constants.LUMENS_ASSET_TYPE,
                  var currAssetName: String = Constants.LUMENS_ASSET_NAME,
                  var currAssetIssuer: String = "") {

    fun getFormattedCurrentAssetCode() : String {
        return if (currAssetCode == Constants.LUMENS_ASSET_TYPE) {
            Constants.LUMENS_ASSET_CODE
        } else {
            currAssetCode
        }
    }

    fun getFormattedCurrentAvailableBalance(): String {
        return "Available: " + AccountUtils.getBalance(currAssetCode) + " " +
                getFormattedCurrentAssetCode()
    }
}