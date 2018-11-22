package com.blockeq.stellarwallet.models

import com.blockeq.stellarwallet.helpers.Constants

class TrustLine(private val numBalances: Int) {
    var count = numBalances - 1
    var amount = count * Constants.MINIMUM_BALANCE_INCREMENT
}