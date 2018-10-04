package blockeq.com.stellarwallet.utils

import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.models.MinimumBalance
import org.stellar.sdk.responses.AccountResponse

class AccountUtils {

    companion object {

        fun getTotalBalance(type : String) : String {
            WalletApplication.localStore!!.balances!!.forEach {
                if (it.assetType == type) {
                    return StringFormat.truncateDecimalPlaces(it.balance)
                } else if (it.assetCode == type) {
                    return StringFormat.truncateDecimalPlaces(it.balance)
                }
            }
            return Constants.DEFAULT_ACCOUNT_BALANCE
        }

        fun getAvailableBalance(account: AccountResponse): String {
            val minimumBalance = MinimumBalance(account)
            return StringFormat.truncateDecimalPlaces(
                    (getTotalBalance(Constants.LUMENS_ASSET_TYPE).toDouble() - minimumBalance.totalAmount).toString())
        }
    }
}
