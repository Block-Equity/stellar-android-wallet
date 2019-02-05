package com.blockeq.stellarwallet.models

import org.stellar.sdk.Asset

interface AssetAvailability {
    val assetCode:String
    val assetIssuer:String?
    val asset: Asset
    val postedForTradeAmount: Float
    val total: Float
    val totalAvailable: Float
}
