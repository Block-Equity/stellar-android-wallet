package com.blockeq.stellarwallet.utils

import android.content.Context
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.encryption.CipherWrapper
import com.blockeq.stellarwallet.encryption.KeyStoreWrapper
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.mvvm.effects.EffectsRepository
import com.soneso.stellarmnemonics.Wallet
import org.stellar.sdk.KeyPair

class AccountUtils {

    companion object {
        private const val CIPHER_TRANSFORMATION : String = "RSA/ECB/PKCS1Padding"

        fun generateWallet (context: Context, mnemonic: String, passphrase: String?, pin : String) {
            AccountUtils.encryptAndStoreWallet(context, mnemonic, passphrase, pin)

            val stellarKeyPair = AccountUtils.getStellarKeyPair(mnemonic, passphrase)

            WalletApplication.wallet.setStellarAccountId(stellarKeyPair.accountId)
            WalletApplication.userSession.setPin(pin)
        }

        fun getSecretSeed(context : Context) : CharArray {
            val encryptedPhrase = WalletApplication.wallet.getEncryptedPhrase()!!
            val encryptedPassphrase = WalletApplication.wallet.getEncryptedPassphrase()
            val masterKey = getPinMasterKey(context, WalletApplication.userSession.getPin()!!)!!

            val decryptedPhrase = getDecryptedString(encryptedPhrase, masterKey)

            var decryptedPassphrase : String? = null
            if (encryptedPassphrase != null) {
                decryptedPassphrase = getDecryptedString(encryptedPassphrase, masterKey)
            }

            return getStellarKeyPair(decryptedPhrase, decryptedPassphrase).secretSeed
        }

        fun encryptAndStoreWallet(context: Context, mnemonic: String, passphrase: String?, pin: String) : Boolean {
            val keyStoreWrapper = KeyStoreWrapper(context)
            keyStoreWrapper.createAndroidKeyStoreAsymmetricKey(pin)

            val masterKey = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin) ?: return false
            val cipherWrapper = CipherWrapper(CIPHER_TRANSFORMATION)
            val encryptedPhrase : String

            if (passphrase == null || passphrase.isEmpty()) {
                encryptedPhrase = cipherWrapper.encrypt(mnemonic, masterKey.public)
            } else {
                WalletApplication.wallet.setEncryptedPassphrase(cipherWrapper.encrypt(passphrase, masterKey.public))
                encryptedPhrase = cipherWrapper.encrypt(mnemonic, masterKey.public)
            }

            WalletApplication.wallet.setEncryptedPhrase(encryptedPhrase)
            return true

        }

        fun getDecryptedString(encryptedPhrase: String, masterKey: java.security.KeyPair) : String {
            val cipherWrapper = CipherWrapper(CIPHER_TRANSFORMATION)
            return cipherWrapper.decrypt(encryptedPhrase, masterKey.private)
        }

        fun getTotalBalance(type : String) : String {
            WalletApplication.wallet.getBalances().forEach {
                if (it.assetType == type) {
                    return it.balance
                } else if (it.assetCode == type) {
                    return it.balance
                }
            }
            return Constants.DEFAULT_ACCOUNT_BALANCE
        }

        fun getPinMasterKey(context : Context, pin: String) : java.security.KeyPair? {
            val keyStoreWrapper = KeyStoreWrapper(context)

            return keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin)
        }

        fun getStellarKeyPair(mnemonic: String, passphrase: String?) : KeyPair {
            return if (WalletApplication.wallet.getIsRecoveryPhrase()) {
                Wallet.createKeyPair(mnemonic.toCharArray(), passphrase?.toCharArray(), Constants.USER_INDEX)
            } else {
                KeyPair.fromSecretSeed(mnemonic)
            }
        }

        fun calculateAvailableBalance(): String {
            val minimumBalance = WalletApplication.userSession.getMinimumBalance()!!
            return (getTotalBalance(Constants.LUMENS_ASSET_TYPE).toDouble() - minimumBalance.totalAmount).toString()
        }
    }
}