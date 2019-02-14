package com.blockeq.stellarwallet.models

/**
 * Class which provides a model for AvailableBalance
 * @constructor Sets all properties of the AvailableBalance
 * @property balance the available balance displayed on WalletFragment
 */
data class AvailableBalance(var assetCode: String, var issuer:String?, var balance: String)
