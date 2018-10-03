package blockeq.com.stellarwallet.models

import blockeq.com.stellarwallet.helpers.Constants
import org.stellar.sdk.responses.AccountResponse

class Signer (signers: Array<AccountResponse.Signer>?) {
    var count = signers?.size ?: 0

    var amount = count * Constants.MINIMUM_BALANCE_INCREMENT
}