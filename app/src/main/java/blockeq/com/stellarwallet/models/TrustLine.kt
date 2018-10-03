package blockeq.com.stellarwallet.models

import blockeq.com.stellarwallet.helpers.Constants

class TrustLine(private val numBalances: Int) {
    var count = numBalances - 1
    var amount = count * Constants.MINIMUM_BALANCE_INCREMENT
}