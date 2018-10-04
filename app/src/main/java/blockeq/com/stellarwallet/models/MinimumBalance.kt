package blockeq.com.stellarwallet.models

import org.stellar.sdk.responses.AccountResponse

data class MinimumBalance(val accountResponse: AccountResponse) {

    companion object {
        const val BASE_RESERVE = 0.5
    }

    var trustlines = TrustLine(accountResponse.balances.size)
    var offers = Offer(accountResponse.subentryCount - trustlines.count)
    var signers = Signer(accountResponse.signers)

    var totalAmount = trustlines.amount + offers.amount + signers.amount + BASE_RESERVE

}
