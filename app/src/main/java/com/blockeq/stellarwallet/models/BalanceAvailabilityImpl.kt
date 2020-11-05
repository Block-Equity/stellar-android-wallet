package com.blockeq.stellarwallet.models

import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.interfaces.BalanceAvailability
import org.stellar.sdk.Asset
import org.stellar.sdk.AssetTypeCreditAlphaNum
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.OfferResponse

class BalanceAvailabilityImpl(private val account: AccountResponse,
                              private val offerList: ArrayList<OfferResponse>) : BalanceAvailability {

    override fun getActiveAssetAvailability(): AssetAvailability {
        val sessionAsset = WalletApplication.userSession.getSessionAsset()
        return if(sessionAsset.assetCode == "native") {
            getNativeAssetAvailability()
        } else {
            getAssetAvailability(sessionAsset.assetCode, sessionAsset.assetIssuer)
        }
    }

    override fun getAccountId(): String {
        return account.accountId
    }

    private val nativeBalance: NativeAssetAvailability

    init {
        val extraSigners = account.signers.size - 1f
        val trustlineCount = account.balances.size -1
        val baseCount = 2
        val baseAmount = baseCount*0.5f
        val trustLinesValue = (trustlineCount) * 0.5f

        val balance = getNativeBalance().balance.toFloat()

        var nativeAsset: Asset? = null
        account.balances.forEach {
            if (it.assetType == "native") {
                nativeAsset = it.asset
            }
        }

        nativeBalance = NativeAssetAvailabilityImpl(
                baseCount,
                baseAmount,
                extraSigners.toInt(), extraSigners*1,
                trustlineCount,
                trustLinesValue,
                offerList.size,
                offerList.size.toFloat()*0.5f,
                getPostedForTradeAmount("native",null),
                balance, "XLM", null, nativeAsset!!)
    }

    override fun getAssetAvailability(assetCode: String, issuer:String): AssetAvailability {
        val balance = getBalanceByAsset(assetCode)
        return AssetAvailabilityImpl(assetCode, issuer, getPostedForTradeAmount(assetCode, issuer),
                balance.balance.toFloat(), balance.asset)
    }

    override fun getNativeAssetAvailability() : NativeAssetAvailability {
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
            val asset = it.selling
            when (asset) {
                is AssetTypeNative -> {
                    if (assetCode == "native" || assetCode == "XLM") {
                       postedForTrade += it.amount.toFloat()
                    }
                }
                is AssetTypeCreditAlphaNum -> {
                    if (assetCode == asset.code
                            && issuer == asset.issuer) {
                        postedForTrade += it.amount.toFloat()
                    }
                }
            }
        }
        return postedForTrade
    }

    override fun getAllBalances(): ArrayList<AssetAvailability> {
        val balances : ArrayList<AssetAvailability> = arrayListOf()
        balances.add(getNativeAssetAvailability())
        account.balances.forEach {
            if (it.assetType != "native") {
                balances.add(getAssetAvailability(it.assetCode, it.assetIssuer))
            }
        }
        return balances
    }
}
