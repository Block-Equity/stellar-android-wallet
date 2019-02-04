package com.blockeq.stellarwallet.models

interface NativeAssetAvailability : AssetAvailability {
    val baseAmount: Float
    val additionalSignersCount: Int
    val additionalSignersAmount: Float
    val trustLinesCount: Int
    val trustLinesAmount: Float
    val openOffersCount: Int
    val openOffersAmount: Float
}