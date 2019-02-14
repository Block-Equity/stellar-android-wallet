package com.blockeq.stellarwallet.encryption

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyStoreWrapperTest {
    private val aliases = arrayOf("1234", "5678", "0987")

    @Test
    fun clear_aliases() {
        val context = InstrumentationRegistry.getTargetContext()
        val keyStoreWrapper = KeyStoreWrapper(context)

        aliases.forEach {
            keyStoreWrapper.createAndroidKeyStoreAsymmetricKey(it)
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