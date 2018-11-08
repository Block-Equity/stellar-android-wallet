package blockeq.com.stellarwallet.models

import android.content.Context
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.utils.ErrorWrapper

class HorizonException(private val transactionResultCode : String?,
                       private val operationsResultCodes: ArrayList<String?>?,
                       private val type: HorizonExceptionType) : Exception() {

    public enum class HorizonExceptionType(val value: Int) {
        SEND(R.string.send_error_message),
        CHANGE_TRUSTLINE(R.string.error_trustline_changed),
        INFLATION(R.string.inflation_set_error),
        LOAD_ACCOUNT(R.string.load_account_error),
        LOAD_EFFECTS(R.string.load_effects_error)
    }

    public fun message(context: Context) : String {
        return if (operationsResultCodes != null && !operationsResultCodes.isEmpty()) {
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
