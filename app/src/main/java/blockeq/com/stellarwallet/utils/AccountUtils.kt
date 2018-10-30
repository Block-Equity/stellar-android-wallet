package blockeq.com.stellarwallet.utils

import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.encryption.CipherWrapper
import blockeq.com.stellarwallet.encryption.KeyStoreWrapper
import blockeq.com.stellarwallet.helpers.Constants
import com.soneso.stellarmnemonics.Wallet
import org.stellar.sdk.KeyPair

class AccountUtils {

    companion object {

        fun getSecretSeed() : CharArray {
            val encryptedPhrase = WalletApplication.localStore!!.encryptedPhrase!!
            val masterKey = getPinMasterKey(WalletApplication.userSession.pin!!)!!

            val decryptedPair = AccountUtils.getDecryptedMnemonicPhrasePair(encryptedPhrase, masterKey)
            val decryptedData = decryptedPair.first
            val passphrase = decryptedPair.second

            return getKeyPair(decryptedData, passphrase).secretSeed
        }

        fun getDecryptedMnemonicPhrasePair(encryptedPhrase: String, masterKey: java.security.KeyPair) : Pair<String, String?> {
            val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")
            var passphrase : String? = null
            val decryptedData = if (WalletApplication.localStore!!.isPassphraseUsed) {
                val decryptedString = cipherWrapper.decrypt(encryptedPhrase, masterKey.private)
                passphrase = decryptedString.substring(decryptedString.lastIndexOf(" ") + 1)
                decryptedString.substring(0, decryptedString.lastIndexOf(" "))
            } else {
                cipherWrapper.decrypt(encryptedPhrase, masterKey.private)
            }

            return Pair(decryptedData, passphrase)
        }

        fun getTotalBalance(type : String) : String {
            WalletApplication.localStore!!.balances!!.forEach {
                if (it.assetType == type) {
                    return StringFormat.truncateDecimalPlaces(it.balance)
                } else if (it.assetCode == type) {
                    return StringFormat.truncateDecimalPlaces(it.balance)
                }
            }
            return Constants.DEFAULT_ACCOUNT_BALANCE
        }

        fun getPinMasterKey(pin: String) : java.security.KeyPair? {
            val keyStoreWrapper = KeyStoreWrapper(WalletApplication.applicationContext())

            return keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin)
        }

        fun getKeyPair(recoveryString: String, passphrase: String?) : KeyPair {
            return if (WalletApplication.localStore!!.isRecoveryPhrase) {
                Wallet.createKeyPair(recoveryString.toCharArray(), passphrase?.toCharArray(), Constants.USER_INDEX)
            } else {
                KeyPair.fromSecretSeed(recoveryString)
            }
        }

        fun calculateAvailableBalance(): String {
            val minimumBalance = WalletApplication.userSession.minimumBalance!!
            return StringFormat.truncateDecimalPlaces(
                    (getTotalBalance(Constants.LUMENS_ASSET_TYPE).toDouble() - minimumBalance.totalAmount).toString())
        }
    }
}
