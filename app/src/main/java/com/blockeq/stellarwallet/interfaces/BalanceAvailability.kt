package com.blockeq.stellarwallet.interfaces

import com.blockeq.stellarwallet.models.AssetAvailability
import com.blockeq.stellarwallet.models.NativeAssetAvailability

interface BalanceAvailability {
    fun getAccountId(): String
    fun getActiveAssetAvailability(): AssetAvailability
    fun getAssetAvailability(assetCode: String, issuer:String): AssetAvailability
    fun getNativeAssetAvailability(): NativeAssetAvailability
    fun getAllBalances(): ArrayList<AssetAvailability>
}
