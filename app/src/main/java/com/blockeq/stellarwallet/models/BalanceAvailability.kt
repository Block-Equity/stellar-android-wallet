package com.blockeq.stellarwallet.models

import org.stellar.sdk.AssetTypeCreditAlphaNum
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.OfferResponse
import timber.log.Timber
import java.util.ArrayList

class BalanceAvailability(private val account: AccountResponse, private val offerList: ArrayList<OfferResponse>) {
    private var baseReserve = 0.5
    private var baseFee = 0.00001
    private val nativeBalance: NativeAssetAvailability

    init {
        val extraSigners = account.signers.size - 1f
        val baseAmount = 2f
        val trustLinesValue = (account.balances.size - 1f) * baseAmount

        val balance = getNativeBalance().balance.toFloat()
        nativeBalance = NativeAssetAvailabilityImpl(
                baseAmount,
                extraSigners.toInt(), extraSigners*1,
                account.balances.size,
                trustLinesValue,
                offerList.size,
                offerList.size.toFloat()*0.5f,
                getPostedForTradeAmount("native",null),
                balance)
    }

    fun getAssetAvailability(assetCode: String, issuer:String): AssetAvailability {
        return AssetAvailabilityImpl(getPostedForTradeAmount(assetCode, issuer),
                getBalanceByAsset(assetCode).balance.toFloat())
    }

    fun getNativeAssetAvailability() : NativeAssetAvailability {
        return nativeBalance
    }

    private fun getNativeBalance() : AccountResponse.Balance {
        account.balances.forEach {
            if (it.assetType == "native") {
                return it
            }
        }
        throw IllegalStateException("native balance not found")
    }

    private fun getBalanceByAsset(assetCode: String): AccountResponse.Balance {
        account.balances.forEach {
            if (it.assetCode == assetCode) {
                return it
            }
        }
        throw IllegalStateException("balance with $assetCode not found")
    }

    private fun getPostedForTradeAmount(assetCode : String, issuer: String?) : Float {
        var postedForTrade = 0f
        offerList.forEach {
            Timber.d("d")
            val asset = it.selling
            when (asset) {
                is AssetTypeNative -> {
                    if (assetCode == "native" || assetCode == "XLM") {
                       postedForTrade += it.amount.toFloat()
                    }
                }
                is AssetTypeCreditAlphaNum -> {
                    if (assetCode == asset.code
                            && issuer == asset.issuer.accountId) {
                        postedForTrade += it.amount.toFloat()
                    }
                }
            }
        }
        return postedForTrade
    }
}
