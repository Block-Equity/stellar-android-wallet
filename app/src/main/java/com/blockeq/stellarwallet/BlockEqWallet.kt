package com.blockeq.stellarwallet

import com.blockeq.stellarwallet.interfaces.LocalStore
import org.stellar.sdk.responses.AccountResponse

class BlockEqWallet(private var localStore: LocalStore) : LocalStore {

    override fun getEncryptedPhrase(): String? {
       return localStore.getEncryptedPhrase()
    }

    override fun setEncryptedPhrase(encryptedPassphrase: String?) {
        localStore.setEncryptedPhrase(encryptedPassphrase)
    }

    override fun getEncryptedPassphrase(): String? {
       return localStore.getEncryptedPassphrase()
    }

    override fun setEncryptedPassphrase(encryptedPassphrase: String) {
      localStore.setEncryptedPassphrase(encryptedPassphrase)
    }

    override fun getStellarAccountId(): String? {
       return localStore.getStellarAccountId()
    }

    override fun setStellarAccountId(accountId: String) {
        localStore.setStellarAccountId(accountId)
    }

    override fun getBalances(): Array<AccountResponse.Balance> {
        return localStore.getBalances()
    }

    override fun setBalances(balances: Array<AccountResponse.Balance>?) {
      localStore.setBalances(balances)
    }

    override fun getAvailableBalance(): String {
      return localStore.getAvailableBalance()
    }

    override fun setAvailableBalance(availableBalance: String?) {
       localStore.setAvailableBalance(availableBalance)
    }

    override fun getIsRecoveryPhrase(): Boolean {
       return localStore.getIsRecoveryPhrase()
    }

    override fun setIsRecoveryPhrase(isRecoveryPhrase: Boolean) {
       localStore.setIsRecoveryPhrase(isRecoveryPhrase)
    }

    override fun setShowPinOnSend(showPinOnSend: Boolean) {
       localStore.setShowPinOnSend(showPinOnSend)
    }

    override fun getShowPinOnSend(): Boolean {
        return localStore.getShowPinOnSend()
    }
}
