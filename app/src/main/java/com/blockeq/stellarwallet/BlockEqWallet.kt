package com.blockeq.stellarwallet

import com.blockeq.stellarwallet.interfaces.CloudNodeStorage
import com.blockeq.stellarwallet.interfaces.LocalStore
import com.blockeq.stellarwallet.interfaces.WalletStore
import com.blockeq.stellarwallet.models.BasicBalance
import org.stellar.sdk.responses.AccountResponse

class BlockEqWallet(private val localStore: LocalStore, private val cloudNode : CloudNodeStorage) : WalletStore {
    override fun setCloudStorageEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            cloudNode.saveAccountId(getStellarAccountId())
            cloudNode.saveBalances(toBasicBalances((getBalances())))
        } else {
            clearCloudStorage()
        }
    }

    override fun clearCloudStorage() {
        cloudNode.clearNode()
    }

    //region {@link LocalStore}
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
        cloudNode.saveAccountId(accountId)
        localStore.setStellarAccountId(accountId)
    }

    override fun getBalances(): Array<AccountResponse.Balance> {
        return localStore.getBalances()
    }

    override fun setBalances(balances: Array<AccountResponse.Balance>?) {
       if (balances != null) {
           cloudNode.saveBalances(toBasicBalances(balances))
       }
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

    override fun clearUserData(): Boolean {
        cloudNode.clearNode()
        return localStore.clearUserData()
    }

    //endregion {@link LocalStore}

    private fun toBasicBalances(balances: Array<AccountResponse.Balance>) : ArrayList<BasicBalance> {
        val simpleBalances = arrayListOf<BasicBalance>()
        balances.forEach { that ->
                var assetCode = that.assetCode
                var issuer : String? = null
                if (assetCode == null) {
                    assetCode = "LMX"
                } else {
                    issuer = that.assetIssuer.accountId
                }
                simpleBalances.add(BasicBalance(that.balance, that.assetType, assetCode, issuer))
            }
        return simpleBalances
    }
}
