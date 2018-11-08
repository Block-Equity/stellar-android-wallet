package blockeq.com.stellarwallet.utils

import android.content.Context
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.helpers.Constants

class ErrorWrapper(context: Context) {
    var map: MutableMap<String, String> = mutableMapOf()

    init {
        map[context.getString(R.string.tx_failed)] = "Transaction failed"
        map[context.getString(R.string.op_underfunded)] = "Payment is underfunded"
    }
}