package blockeq.com.stellarwallet.utils

import android.content.Context
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.encryption.CipherWrapper
import blockeq.com.stellarwallet.encryption.KeyStoreWrapper
import blockeq.com.stellarwallet.helpers.Constants
import com.soneso.stellarmnemonics.Wallet
import org.stellar.sdk.KeyPair
import java.security.PrivateKey

class AccountUtils {

    companion object {
        private const val CIPHER_TRANSFORMATION : String = "RSA/ECB/PKCS1Padding"

        fun getSecretSeed(context : Context) : CharArray {
            val encryptedPhrase = WalletApplication.localStore.encryptedPhrase!!
            val masterKey = getPinMasterKey(context, WalletApplication.userSession.pin!!)!!

            val decryptedPair = getDecryptedMnemonicPhrasePair(encryptedPhrase, masterKey.private)
            val decryptedData = decryptedPair.first
            val passphrase = decryptedPair.second

            return getStellarKeyPair(decryptedData, passphrase).secretSeed
        }

        fun getEncryptedMnemonicPhrase(context: Context, mnemonic: String, passphrase: String?, pin: String) : String {
            val keyStoreWrapper = KeyStoreWrapper(context)
            keyStoreWrapper.createAndroidKeyStoreAsymmetricKey(pin)

            val masterKey = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin)
            val cipherWrapper = CipherWrapper(CIPHER_TRANSFORMATION)

            return if (passphrase.isNullOrEmpty()) {
                WalletApplication.localStore.isPassphraseUsed = false
                cipherWrapper.encrypt(mnemonic, masterKey?.public)
            } else {
                WalletApplication.localStore.isPassphraseUsed = true
                cipherWrapper.encrypt("$mnemonic $passphrase", masterKey?.public)
            }
        }

        fun getDecryptedMnemonicPhrasePair(encryptedPhrase: String, privateKey: PrivateKey) : Pair<String, String?> {
            val cipherWrapper = CipherWrapper(CIPHER_TRANSFORMATION)
            var passphrase : String? = null
            val decryptedData = if (WalletApplication.localStore.isPassphraseUsed) {
                val decryptedString = cipherWrapper.decrypt(encryptedPhrase, privateKey)
                passphrase = decryptedString.substring(decryptedString.lastIndexOf(" ") + 1)
                decryptedString.substring(0, decryptedString.lastIndexOf(" "))
            } else {
                cipherWrapper.decrypt(encryptedPhrase, privateKey)
            }

            return Pair(decryptedData, passphrase)
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
    }
}