package com.blockeq.stellarwallet.utils

import android.content.Context
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.encryption.CipherWrapper
import com.blockeq.stellarwallet.encryption.KeyStoreWrapper
import com.blockeq.stellarwallet.helpers.Constants
import com.soneso.stellarmnemonics.Wallet
import org.stellar.sdk.KeyPair
import java.security.PrivateKey

class AccountUtils {

    companion object {
        private const val CIPHER_TRANSFORMATION : String = "RSA/ECB/PKCS1Padding"

        fun getSecretSeed(context : Context) : CharArray {
            val encryptedPhrase = WalletApplication.localStore.encryptedPhrase!!
            val encryptedPassphrase = WalletApplication.localStore.encryptedPassphrase
            val masterKey = getPinMasterKey(context, WalletApplication.userSession.pin!!)!!

            var decryptedPhrase = getDecryptedString(encryptedPhrase, masterKey)

            var decryptedPassphrase : String? = null
            if (encryptedPassphrase != null) {
                decryptedPassphrase = getDecryptedString(encryptedPassphrase, masterKey)
            }

            // TODO: Remove for new app, this is purely passphrase migration code
            if (isOldWalletWithPassphrase()) {
                val decryptedPair = AccountUtils.getOldDecryptedPair(encryptedPhrase, masterKey.private)
                decryptedPhrase = decryptedPair.first
                decryptedPassphrase = decryptedPair.second
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
                if (AccountUtils.isOldWalletWithPassphrase()) {
                    encryptedPhrase = cipherWrapper.encrypt("$mnemonic $passphrase", masterKey.public)
                } else {
                    WalletApplication.localStore.encryptedPassphrase = cipherWrapper.encrypt(passphrase, masterKey.public)
                    encryptedPhrase = cipherWrapper.encrypt(mnemonic, masterKey.public)
                }
            }

            WalletApplication.localStore.encryptedPhrase = encryptedPhrase
            return true

        }

        fun getDecryptedString(encryptedPhrase: String, masterKey: java.security.KeyPair) : String {
            val cipherWrapper = CipherWrapper(CIPHER_TRANSFORMATION)
            return cipherWrapper.decrypt(encryptedPhrase, masterKey.private)
        }

        @Deprecated("TODO: Remove this method in new app")
        fun getOldDecryptedPair(encryptedPhrase: String, privateKey: PrivateKey) : Pair<String, String?> {
            val cipherWrapper = CipherWrapper(CIPHER_TRANSFORMATION)
            var passphrase : String? = null
            val mnemonic: String

            val decryptedString = cipherWrapper.decrypt(encryptedPhrase, privateKey)

            val wordCount = StringFormat.getWordCount(decryptedString)

            // Check for inconsistent state of isPassphraseUsed
            if ((wordCount == 12 || wordCount == 24) && WalletApplication.localStore.isPassphraseUsed) {
                WalletApplication.localStore.isPassphraseUsed = false
            }

            if (WalletApplication.localStore.isPassphraseUsed) {
                val words = decryptedString.split(" ".toRegex()).dropLastWhile { it.isEmpty() } as ArrayList

                val range = if (wordCount <= 24) {
                    0..11
                } else {
                    0..23
                }

                var index = 0
                for (i in range) {
                    index += words[i].length + 1
                }

                mnemonic = decryptedString.substring(0, index - 1)
                passphrase = decryptedString.substring(index)
            } else {
                mnemonic = cipherWrapper.decrypt(encryptedPhrase, privateKey)
            }

            return Pair(mnemonic, passphrase)
        }

        @Deprecated("TODO: Remove this method in new app")
        fun isOldWalletWithPassphrase() : Boolean {
            return WalletApplication.localStore.isPassphraseUsed && WalletApplication.localStore.encryptedPassphrase == null && WalletApplication.localStore.encryptedPhrase != null
        }

        fun getTotalBalance(type : String) : String {
            WalletApplication.localStore.balances!!.forEach {
                if (it.assetType == type) {
                    return StringFormat.truncateDecimalPlaces(it.balance)
                } else if (it.assetCode == type) {
                    return StringFormat.truncateDecimalPlaces(it.balance)
                }
            }
            return Constants.DEFAULT_ACCOUNT_BALANCE
        }

        fun getPinMasterKey(context : Context, pin: String) : java.security.KeyPair? {
            val keyStoreWrapper = KeyStoreWrapper(context)

            return keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin)
        }

        fun getStellarKeyPair(mnemonic: String, passphrase: String?) : KeyPair {
            return if (WalletApplication.localStore.isRecoveryPhrase) {
                Wallet.createKeyPair(mnemonic.toCharArray(), passphrase?.toCharArray(), Constants.USER_INDEX)
            } else {
                KeyPair.fromSecretSeed(mnemonic)
            }
        }

        fun calculateAvailableBalance(): String {
            val minimumBalance = WalletApplication.userSession.minimumBalance!!
            return StringFormat.truncateDecimalPlaces(
                    (getTotalBalance(Constants.LUMENS_ASSET_TYPE).toDouble() - minimumBalance.totalAmount).toString())
        }

        fun wipe(context: Context) : Boolean {
            val keyStoreWrapper = KeyStoreWrapper(context)
            keyStoreWrapper.clear()
            return WalletApplication.localStore.clearUserData()
        }
    }
}