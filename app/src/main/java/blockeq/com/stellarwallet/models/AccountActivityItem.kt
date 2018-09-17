package blockeq.com.stellarwallet.models

enum class TransactionType {
    SEND, RECEIVE
}

data class AccountActivityItem (var activityType: TransactionType, var amount: String)
