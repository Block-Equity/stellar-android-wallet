package blockeq.com.stellarwallet.helpers

import blockeq.com.stellarwallet.BuildConfig
import com.soneso.stellarmnemonics.WalletException
import com.soneso.stellarmnemonics.derivation.Ed25519Derivation
import com.soneso.stellarmnemonics.mnemonic.MnemonicException
import com.soneso.stellarmnemonics.util.PrimitiveUtil
import org.stellar.sdk.KeyPair
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class SupportedMnemonic {
    companion object {
        const val ALGORITHM_API_19 = "PBKDF2withHmacSHA1And8BIT"
        const val ALGORITHM_API_26 = "PBKDF2WithHmacSHA512"

        @Throws(MnemonicException::class)
        fun createSeed(mnemonic: CharArray, passphrase: CharArray?): ByteArray {
            var saltChars = charArrayOf('m', 'n', 'e', 'm', 'o', 'n', 'i', 'c')
            if (passphrase != null) {
                saltChars = PrimitiveUtil.concatCharArrays(saltChars, passphrase)
            }

            val salt = PrimitiveUtil.toBytes(saltChars)

            try {
                val ks = PBEKeySpec(mnemonic, salt, 2048, 512)
                val skf = if (BuildConfig.VERSION_CODE >= 26) {
                    SecretKeyFactory.getInstance(ALGORITHM_API_26)
                } else {
                    SecretKeyFactory.getInstance(ALGORITHM_API_19)
                }
                return skf.generateSecret(ks).encoded
            } catch (var6: Exception) {
                throw MnemonicException("Fatal error when generating seed from mnemonic!")
            }
        }

        @Throws(WalletException::class)
        fun createKeyPair(mnemonic: CharArray, passphrase: CharArray?, index: Int): KeyPair {
            val masterSeed = createSeed(mnemonic, passphrase)

            val masterPrivateKey = Ed25519Derivation.fromSecretSeed(masterSeed)
            val purpose = masterPrivateKey.derived(44)
            val coinType = purpose.derived(148)
            val account = coinType.derived(index)
            return KeyPair.fromSecretSeed(account.privateKey)
        }
    }
}