package com.blockeq.stellarwallet.interfaces

import org.stellar.sdk.responses.AccountResponse

interface LocalStore {
    fun getEncryptedPhrase(): String?
    fun setEncryptedPhrase(encryptedPassphrase: String?)
    fun getEncryptedPassphrase(): String?
    fun setEncryptedPassphrase(encryptedPassphrase: String)
    fun getStellarAccountId(): String?
    fun setStellarAccountId(accountId: String)
    fun getBalances(): Array<AccountResponse.Balance>
    fun setBalances(balances: Array<AccountResponse.Balance>?)
    fun getAvailableBalance(): String
    fun setAvailableBalance(availableBalance: String?)
    fun getIsRecoveryPhrase(): Boolean
    fun setIsRecoveryPhrase(isRecoveryPhrase: Boolean)
    fun setShowPinOnSend(showPinOnSend: Boolean)
    fun getShowPinOnSend(): Boolean
    fun clearLocalStore():  Boolean
}