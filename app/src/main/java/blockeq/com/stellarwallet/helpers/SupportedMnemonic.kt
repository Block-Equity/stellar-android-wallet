package blockeq.com.stellarwallet.helpers

import android.os.AsyncTask
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.interfaces.OnWalletSeedCreated
import blockeq.com.stellarwallet.models.Session
import com.soneso.stellarmnemonics.WalletException
import com.soneso.stellarmnemonics.derivation.Ed25519Derivation
import com.soneso.stellarmnemonics.mnemonic.MnemonicException
import com.soneso.stellarmnemonics.util.PrimitiveUtil
import org.spongycastle.crypto.PBEParametersGenerator
import org.spongycastle.crypto.digests.SHA512Digest
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.spongycastle.crypto.params.KeyParameter
import org.stellar.sdk.KeyPair


class SupportedMnemonic {
    companion object {

        const val USER_INDEX = 0

        class GenerateStellarAddressTask(private val listener: OnWalletSeedCreated) : AsyncTask<String, Void, KeyPair>() {

            override fun doInBackground(vararg mnemonic: String) : KeyPair? {
                val keyPair = SupportedMnemonic.createKeyPair(mnemonic[0].toCharArray(), null, USER_INDEX)
                WalletApplication.localStore!!.publicKey = keyPair.accountId
                WalletApplication.session = Session(keyPair)
                return keyPair
            }

            override fun onPostExecute(keyPair: KeyPair?) {
                listener.onWalletSeedCreated(keyPair)
            }
        }


        @Throws(MnemonicException::class)
        fun createSeed(mnemonic: CharArray, passphrase: CharArray?): ByteArray {
            var saltChars = charArrayOf('m', 'n', 'e', 'm', 'o', 'n', 'i', 'c')
            if (passphrase != null) {
                saltChars = PrimitiveUtil.concatCharArrays(saltChars, passphrase)
            }

            val salt = PrimitiveUtil.toBytes(saltChars)

            try {
                val generator = PKCS5S2ParametersGenerator(SHA512Digest())
                generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(mnemonic), salt, 2048)
                val key = generator.generateDerivedMacParameters(512) as KeyParameter
                return key.key

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