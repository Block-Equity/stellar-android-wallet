package blockeq.com.stellarwallet.encryption

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import blockeq.com.stellarwallet.utils.AccountUtils
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyStoreWrapperTest {
    val aliases = arrayOf("1234", "5678", "0987")
    val mnemonic = arrayOf("abandon",
            "ability",
            "able",
            "about",
            "above",
            "absent",
            "absorb",
            "abstract",
            "absurd",
            "abuse",
            "access",
            "accident")

    @Test
    fun clear_aliases() {
        val context = InstrumentationRegistry.getTargetContext()
        val keyStoreWrapper = KeyStoreWrapper(context)

        aliases.forEach {
            AccountUtils.getEncryptedMnemonicPhrase(context, mnemonic.joinToString(" "), null, it)
        }

        aliases.forEach {
            assert(keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(it) != null)
        }

        keyStoreWrapper.clear()

        aliases.forEach {
            assert(keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(it) == null)
        }
    }
}