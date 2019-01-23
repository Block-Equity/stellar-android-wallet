package com.blockeq.stellarwallet.models

/**
 * Class which provides a model for TotalBalance
 * @constructor Sets all properties of the TotalBalance
 * @property balance the total balance displayed on WalletFragment
 */
data class TotalBalance (var assetName: String, var assetCode: String, var balance: String)
