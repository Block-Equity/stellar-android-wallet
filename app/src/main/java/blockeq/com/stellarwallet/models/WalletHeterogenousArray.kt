package blockeq.com.stellarwallet.models

import org.stellar.sdk.responses.effects.EffectResponse

class WalletHeterogenousArray(totalBalance: TotalBalance, availableBalance: AvailableBalance,
                              pair: Pair<*, *>, effectsList: ArrayList<EffectResponse>) : ArrayList<Any>() {

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
        array.add(effectsList)
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
        array.add(list)
    }

    //endregion

}
