package blockeq.com.stellarwallet.utils

import android.content.Context
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.encryption.CipherWrapper
import blockeq.com.stellarwallet.encryption.KeyStoreWrapper
import blockeq.com.stellarwallet.helpers.Constants
import com.soneso.stellarmnemonics.Wallet
import org.stellar.sdk.KeyPair

class AccountUtils {

    companion object {

        fun getSecretSeed(context : Context) : CharArray {
            val encryptedPhrase = WalletApplication.localStore.encryptedPhrase!!
            val masterKey = getPinMasterKey(context, WalletApplication.userSession.pin!!)

            val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")
            return getKeyPair(cipherWrapper.decrypt(encryptedPhrase, masterKey!!.private)).secretSeed
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

        fun getKeyPair(string : String) : KeyPair {
            return if (WalletApplication.localStore.isRecoveryPhrase) {
                Wallet.createKeyPair(string.toCharArray(), null, Constants.USER_INDEX)
            } else {
                KeyPair.fromSecretSeed(string)
            }
        }

        fun calculateAvailableBalance(): String {
            val minimumBalance = WalletApplication.userSession.minimumBalance!!
            return StringFormat.truncateDecimalPlaces(
                    (getTotalBalance(Constants.LUMENS_ASSET_TYPE).toDouble() - minimumBalance.totalAmount).toString())
        }
    }
}
