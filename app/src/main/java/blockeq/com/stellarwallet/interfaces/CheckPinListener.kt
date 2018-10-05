package blockeq.com.stellarwallet.interfaces

import org.stellar.sdk.Asset

interface CheckPinListener {
    fun checkPin(asset: Asset, isRemoveAsset: Boolean)
}