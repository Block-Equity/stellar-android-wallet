package com.blockeq.stellarwallet.models

import com.blockeq.stellarwallet.helpers.Constants

class Offer (var count: Int) {
    var amount = count * Constants.MINIMUM_BALANCE_INCREMENT
}