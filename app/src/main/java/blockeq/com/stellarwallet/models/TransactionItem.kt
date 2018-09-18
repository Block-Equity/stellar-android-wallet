package blockeq.com.stellarwallet.models

enum class TransactionType(val value : String) {
    SEND("Send"), RECEIVE("Receive")
}

data class TransactionItem (var transactionType: TransactionType, var amount: String, var date : String)
