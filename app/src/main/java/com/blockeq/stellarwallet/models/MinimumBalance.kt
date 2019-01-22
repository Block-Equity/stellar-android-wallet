package com.blockeq.stellarwallet.models

import com.blockeq.stellarwallet.helpers.Constants
import org.stellar.sdk.responses.AccountResponse

data class MinimumBalance(val accountResponse: AccountResponse) {

    var trustlines = TrustLine(accountResponse.balances.size)
    var offers = Offer(accountResponse.subentryCount - trustlines.count)
    var signers = Signer(accountResponse.signers)

    var totalAmount = trustlines.amount + offers.amount + signers.amount + Constants.BASE_RESERVE

}
