package com.blockeq.stellarwallet.utils;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyPair;

import com.blockeq.stellarwallet.WalletApplication;
import com.blockeq.stellarwallet.encryption.CipherWrapper;
import com.blockeq.stellarwallet.encryption.KeyStoreWrapper;
import kotlin.Pair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class AccountUtilsTest {
    private String mnemonic12;
    private String mnemonic24;
    private Context context;
    private String pin = "1234";
    private String passPhrase = "this_is_a_passphrase";

    @Before
    public void before(){
        String[] mnemonicWords12 = {
                "abandon",
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
                "accident"
        };

        String[] mnemonicWords24 = {
                "abandon",
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
                "accident",
                "account",
                "accuse",
                "achieve",
                "acid",
                "acoustic",
                "acquire",
                "across",
                "act",
                "action",
                "actor",
                "actress",
                "actual"
        };

        mnemonic12 = TextUtils.join(" ", mnemonicWords12);
        mnemonic24 = TextUtils.join(" ", mnemonicWords24);
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void basic_encryption_mnemonic() {
        AccountUtils.Companion.encryptAndStoreWallet(context, mnemonic12, null, pin);
        String phrase = WalletApplication.localStore.getEncryptedPhrase();

        assertNotNull(phrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        Pair<String, String> decryptedPair = AccountUtils.Companion.getOldDecryptedPair(phrase, keyPair.getPrivate());
        assertNull(decryptedPair.component2());
        assertEquals(decryptedPair.component1(), mnemonic12);
    }

    @Test
    public void basic_encryption_mnemonic_with_passphrase() {
        AccountUtils.Companion.encryptAndStoreWallet(context, mnemonic12, passPhrase, pin);
        String phrase = WalletApplication.localStore.getEncryptedPhrase();
        assertNotNull(phrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        String decryptedPhrase = AccountUtils.Companion.getDecryptedString(phrase, keyPair);
        String encryptedPassphrase = WalletApplication.localStore.getEncryptedPassphrase();
        String decryptedPassphrase = null;
        if (encryptedPassphrase != null) {
            decryptedPassphrase = AccountUtils.Companion.getDecryptedString(encryptedPassphrase, keyPair);
        }

        assertEquals(mnemonic12, decryptedPhrase);
        assertEquals(passPhrase, decryptedPassphrase);
    }


    @Test
    public void basic_encryption_mnemonic12_with_pass_phrase_with_spaces() {
        String passphrase = "passphrase with spaces";

        AccountUtils.Companion.encryptAndStoreWallet(context, mnemonic12, passphrase, pin);
        String phrase = WalletApplication.localStore.getEncryptedPhrase();
        assertNotNull(phrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        String decryptedPhrase = AccountUtils.Companion.getDecryptedString(phrase, keyPair);
        String encryptedPassphrase = WalletApplication.localStore.getEncryptedPassphrase();
        String decryptedPassphrase = null;
        if (encryptedPassphrase != null) {
            decryptedPassphrase = AccountUtils.Companion.getDecryptedString(encryptedPassphrase, keyPair);
        }

        assertEquals(mnemonic12, decryptedPhrase);
        assertEquals(passphrase, decryptedPassphrase);
    }

    @Test
    public void basic_encryption_mnemonic24_with_pass_phrase_with_spaces() {
        String passphrase = "passphrase with spaces";

        AccountUtils.Companion.encryptAndStoreWallet(context, mnemonic24, passphrase, pin);
        String phrase = WalletApplication.localStore.getEncryptedPhrase();
        assertNotNull(phrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        String decryptedPhrase = AccountUtils.Companion.getDecryptedString(phrase, keyPair);
        String encryptedPassphrase = WalletApplication.localStore.getEncryptedPassphrase();
        String decryptedPassphrase = null;
        if (encryptedPassphrase != null) {
            decryptedPassphrase = AccountUtils.Companion.getDecryptedString(encryptedPassphrase, keyPair);
        }

        assertEquals(mnemonic24, decryptedPhrase);
        assertEquals(passphrase, decryptedPassphrase);
    }

    // TODO: Remove in new app
    // Creates a passphrase wallet with the <= 1.0.3 version
    // ensures that the user can login and use the app on future versions
    @Test
    public void backwards_compatibility_test_login() {
        // region Set up
        // Conditions for an old wallet (<= 1.0.3 SNAPSHOT)

        KeyStoreWrapper keyStoreWrapper = new KeyStoreWrapper(context);
        keyStoreWrapper.createAndroidKeyStoreAsymmetricKey(pin);

        KeyPair masterKey = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin);
        CipherWrapper cipherWrapper = new CipherWrapper("RSA/ECB/PKCS1Padding");

        assert masterKey != null;
        String encryptedPhrase = cipherWrapper.encrypt(mnemonic12 + " "  + passPhrase, masterKey.getPublic(), false);

        WalletApplication.localStore.setEncryptedPhrase(encryptedPhrase);
        WalletApplication.localStore.setPassphraseUsed(true);
        WalletApplication.localStore.setEncryptedPassphrase(null);

        // endregion

        encryptedPhrase = WalletApplication.localStore.getEncryptedPhrase();
        assertNotNull(encryptedPhrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        String decryptedPhrase = AccountUtils.Companion.getDecryptedString(encryptedPhrase, keyPair);
        String encryptedPassphrase = WalletApplication.localStore.getEncryptedPassphrase();
        String decryptedPassphrase = null;
        if (encryptedPassphrase != null) {
            decryptedPassphrase = AccountUtils.Companion.getDecryptedString(encryptedPassphrase, keyPair);
        }

        if (AccountUtils.Companion.isOldWalletWithPassphrase()) {
            Pair<String, String> decryptedPair = AccountUtils.Companion.getOldDecryptedPair(encryptedPhrase, keyPair.getPrivate());
            decryptedPhrase = decryptedPair.getFirst();
            decryptedPassphrase = decryptedPair.getSecond();
        }

        assertEquals(mnemonic12, decryptedPhrase);
        assertEquals(passPhrase, decryptedPassphrase);
    }

    @After
    public void cleanUp() {
        if (!AccountUtils.Companion.wipe(context)) {
            throw new IllegalStateException("failed to wipe");
        }
    }
}
