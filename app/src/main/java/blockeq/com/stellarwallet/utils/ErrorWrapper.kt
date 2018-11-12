package blockeq.com.stellarwallet.utils

import android.content.Context
import blockeq.com.stellarwallet.R

class ErrorWrapper(context: Context) {
    var map: MutableMap<String, String> = mutableMapOf()

    init {
        map[context.getString(R.string.technical_tx_failed)] = context.getString(R.string.readable_tx_failed)
        map[context.getString(R.string.technical_tx_insufficient_balance)] = context.getString(R.string.readable_tx_insufficient_balance)
        map[context.getString(R.string.technical_op_underfunded)] = context.getString(R.string.readable_op_underfunded)
        map[context.getString(R.string.technical_op_low_reserve)] = context.getString(R.string.readable_op_low_reserve)
    }
}