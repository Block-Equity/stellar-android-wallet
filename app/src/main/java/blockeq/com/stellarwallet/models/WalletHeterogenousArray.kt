package blockeq.com.stellarwallet.models

import org.stellar.sdk.AssetTypeCreditAlphaNum
import org.stellar.sdk.AssetTypeCreditAlphaNum12
import org.stellar.sdk.AssetTypeCreditAlphaNum4
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.responses.effects.AccountCreditedEffectResponse
import org.stellar.sdk.responses.effects.AccountDebitedEffectResponse
import org.stellar.sdk.responses.effects.EffectResponse
import org.stellar.sdk.responses.effects.TradeEffectResponse
import org.stellar.sdk.xdr.AssetType

class WalletHeterogenousArray(totalBalance: TotalBalance, availableBalance: AvailableBalance,
                              pair: Pair<*, *>, effectsList: ArrayList<EffectResponse>?) : ArrayList<Any>() {

    companion object {
        const val TOTAL_INDEX = 0
        const val AVAILABLE_INDEX = 1
        const val PAIR_INDEX = 2
        const val EFFECTS_LIST_INDEX = 3
    }

    var array: ArrayList<Any> = ArrayList()

    init {
        array.add(totalBalance)
        array.add(availableBalance)
        array.add(pair)
        addFilteredEffects(effectsList)
    }

    //region Update methods

    fun updateTotalBalance(balance: TotalBalance) {
        array.removeAt(TOTAL_INDEX)
        array.add(TOTAL_INDEX, balance)
    }

    fun updateAvailableBalance(balance: AvailableBalance) {
        array.removeAt(AVAILABLE_INDEX)
        array.add(AVAILABLE_INDEX, balance)
    }

    fun updatePair(p: Pair<*, *>) {
        array.removeAt(PAIR_INDEX)
        array.add(PAIR_INDEX, p)
    }

    fun updateEffectsList(list: ArrayList<EffectResponse>) {
        array.subList(EFFECTS_LIST_INDEX, array.size).clear()
        addFilteredEffects(list)
    }

    //endregion

    private fun addFilteredEffects(list: ArrayList<EffectResponse>?) {
        val filteredEffects = getFilteredEffects(list, AssetType.ASSET_TYPE_NATIVE)
        if (filteredEffects != null) {
            array.add(convertEffectsToAccountEffects(filteredEffects))
        }
    }

    private fun getFilteredEffects(list: ArrayList<EffectResponse>?, assetType: AssetType) : ArrayList<EffectResponse>? {
        if (list == null) return null

        return (list.filter {
            (it.type == EffectType.RECEIVED.value && (it as AccountCreditedEffectResponse).asset == assetType) ||
            (it.type == EffectType.SENT.value && (it as AccountDebitedEffectResponse).asset == assetType) ||
            (it.type != EffectType.RECEIVED.value && it.type != EffectType.SENT.value)
        } as ArrayList)
        //TODO: Map -> cast to concrete subclasses
    }

    private fun convertEffectsToAccountEffects(list: ArrayList<EffectResponse>) : ArrayList<Any> {
        return list.map {
            if (it is TradeEffectResponse) {
                TradeEffect(it.type, it.createdAt, getBoughtAsset(it), getSoldAsset(it),
                        it.boughtAmount, it.soldAmount)
            } else {
                AccountEffect(it.type, it.createdAt, getAssetCode(it), getAmount(it))
            }
        } as ArrayList
    }

    private fun getAssetCode(effect: EffectResponse): String? {
       if (effect is AccountCreditedEffectResponse) {
           if (effect.asset is AssetTypeCreditAlphaNum) {
               return (effect.asset as AssetTypeCreditAlphaNum).code

           } else if (effect.asset is AssetTypeCreditAlphaNum4) {
               return (effect.asset as AssetTypeCreditAlphaNum4).code

           } else if (effect.asset is AssetTypeCreditAlphaNum12) {
               return (effect.asset as AssetTypeCreditAlphaNum12).code

           } else {
               return (effect.asset as AssetTypeNative).type
           }
       } else if (effect is AccountDebitedEffectResponse) {
           if (effect.asset is AssetTypeCreditAlphaNum) {
               return (effect.asset as AssetTypeCreditAlphaNum).code

           } else if (effect.asset is AssetTypeCreditAlphaNum4) {
               return (effect.asset as AssetTypeCreditAlphaNum4).code

           } else if (effect.asset is AssetTypeCreditAlphaNum12) {
               return (effect.asset as AssetTypeCreditAlphaNum12).code

           } else {
               return (effect.asset as AssetTypeNative).type
           }
       } else {
           return null
       }
    }

    private fun getAmount(effectResponse: EffectResponse) : String? {
        if (effectResponse is AccountCreditedEffectResponse) {
            return effectResponse.amount
        } else if (effectResponse is AccountDebitedEffectResponse) {
            return effectResponse.amount
        } else {
            return null
        }
    }

    private fun getBoughtAsset(trade: TradeEffectResponse) : String {
        if (trade.boughtAsset is AssetTypeCreditAlphaNum) {
            return (trade.boughtAsset as AssetTypeCreditAlphaNum).code

        } else if (trade.boughtAsset is AssetTypeCreditAlphaNum4) {
            return (trade.boughtAsset as AssetTypeCreditAlphaNum4).code

        } else if (trade.boughtAsset is AssetTypeCreditAlphaNum12) {
            return (trade.boughtAsset as AssetTypeCreditAlphaNum12).code

        } else {
            return (trade.boughtAsset as AssetTypeNative).type
        }
    }

    private fun getSoldAsset(trade: TradeEffectResponse) : String {
        if (trade.soldAsset is AssetTypeCreditAlphaNum) {
            return (trade.soldAsset as AssetTypeCreditAlphaNum).code

        } else if (trade.soldAsset is AssetTypeCreditAlphaNum4) {
            return (trade.soldAsset as AssetTypeCreditAlphaNum4).code

        } else if (trade.soldAsset is AssetTypeCreditAlphaNum12) {
            return (trade.soldAsset as AssetTypeCreditAlphaNum12).code

        } else {
            return (trade.soldAsset as AssetTypeNative).type
        }
    }

}
