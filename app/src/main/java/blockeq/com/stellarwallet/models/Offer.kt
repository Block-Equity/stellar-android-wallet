package blockeq.com.stellarwallet.models

import blockeq.com.stellarwallet.helpers.Constants

class Offer (var count: Int) {
    var amount = count * Constants.MINIMUM_BALANCE_INCREMENT
}