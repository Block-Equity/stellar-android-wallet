package com.blockeq.stellarwallet.models

interface NativeAssetAvailability : AssetAvailability {
    val baseAmount: Float
    val additionalSignersAmount: Float
    val trustLinesAmount: Float
    val openOffersAmount: Float
}