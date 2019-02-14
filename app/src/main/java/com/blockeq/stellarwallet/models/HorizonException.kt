package com.blockeq.stellarwallet.models

import android.content.Context
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.utils.ErrorWrapper

class HorizonException(private val transactionResultCode: String,
                       private val operationsResultCodes: ArrayList<String?>?,
                       private val type: HorizonExceptionType) : Exception() {

    enum class HorizonExceptionType(val value: Int) {
        SEND(R.string.send_error_message),
        CHANGE_TRUST_LINE(R.string.error_trustline_changed),
        INFLATION(R.string.inflation_set_error),
        LOAD_ACCOUNT(R.string.load_account_error),
        LOAD_EFFECTS(R.string.load_effects_error)
    }

    fun message(context: Context) : String {
        return if (operationsResultCodes != null && !operationsResultCodes.isEmpty() && operationsResultCodes[0] != null) {
            String.format(context.getString(R.string.standard_error_message),
                    ErrorWrapper(context).map[transactionResultCode],
                    ErrorWrapper(context).map[operationsResultCodes[0]])
        } else {
            String.format(context.getString(R.string.standard_error_message),
                    ErrorWrapper(context).map[transactionResultCode],
                    context.getString(type.value))
        }
    }
}