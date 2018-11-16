package blockeq.com.stellarwallet.utils;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyPair;

import blockeq.com.stellarwallet.WalletApplication;
import blockeq.com.stellarwallet.encryption.CipherWrapper;
import blockeq.com.stellarwallet.encryption.KeyStoreWrapper;
import kotlin.Pair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class AccountUtilsTest {
    private String[] mnemonic = {
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

    private String pin = "1234";

    @Test
    public void basic_encryption_mnemonic() {
        Context context = InstrumentationRegistry.getTargetContext();
        String mnemonicString =  String.join(" ", mnemonic);

        AccountUtils.Companion.encryptAndStoreWallet(InstrumentationRegistry.getTargetContext(), mnemonicString, null, pin);
        String phrase = WalletApplication.localStore.getEncryptedPhrase();
        assertNotNull(phrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        Pair<String, String> decryptedPair = AccountUtils.Companion.getOldDecryptedPair(phrase, keyPair.getPrivate());
        assertNull(decryptedPair.component2());
        assertEquals(decryptedPair.component1(), mnemonicString);
    }

    @Test
    public void basic_encryption_mnemonic_with_passphrase() {
        Context context = InstrumentationRegistry.getTargetContext();
        String mnemonicString =  String.join(" ", mnemonic);
        String passPhrase = "this_is_a_passphrase";

        AccountUtils.Companion.encryptAndStoreWallet(InstrumentationRegistry.getTargetContext(), mnemonicString, passPhrase, pin);
        String phrase = WalletApplication.localStore.getEncryptedPhrase();
        assertNotNull(phrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        Pair<String, String> decryptedPair = AccountUtils.Companion.getOldDecryptedPair(phrase, keyPair.getPrivate());
        assertEquals(passPhrase, decryptedPair.component2());
        assertEquals(decryptedPair.component1(), mnemonicString);
    }


    //TODO: https://github.com/Block-Equity/stellar-android-wallet/issues/74
    //    @Test
    //    public void basic_encryption_mnemonic_with_pass_phrase_with_spaces() {
    //        Context context = InstrumentationRegistry.getTargetContext();
    //        String mnemonicString =  String.join(" ", mnemonic);
    //        String passPhrase = "this is a passphrase";
    //
    //        String phrase = AccountUtils.Companion.encryptAndStoreWallet(InstrumentationRegistry.getTargetContext(), mnemonicString, passPhrase, pin);
    //        assertNotNull(phrase);
    //
    //        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
    //        assertNotNull(keyPair);
    //
    //        Pair<String, String> decryptedPair = AccountUtils.Companion.getOldDecryptedPair(phrase, keyPair.getPrivate());
    //        assertEquals(passPhrase, decryptedPair.component2());
    //        assertEquals(decryptedPair.component1(), mnemonicString);
    //    }

    // TODO: Remove in new app
    // Creates a passphrase wallet with the <= 1.0.3 version
    // ensures that the user can login and use the app on future versions
    @Test
    public void backwards_compatibility_test_login() {
        Context context = InstrumentationRegistry.getTargetContext();
        String mnemonicString =  String.join(" ", mnemonic);
        String passphrase = "this_is_a_passphrase";
        String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

        // region Set up
        // Conditions for an old wallet (<= 1.0.3 SNAPSHOT)

        KeyStoreWrapper keyStoreWrapper = new KeyStoreWrapper(context);
        keyStoreWrapper.createAndroidKeyStoreAsymmetricKey(pin);

        KeyPair masterKey = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin);
        CipherWrapper cipherWrapper = new CipherWrapper(CIPHER_TRANSFORMATION);

        assert masterKey != null;
        String encryptedPhrase = cipherWrapper.encrypt(String.join(" ", mnemonic) + " "  + passphrase, masterKey.getPublic(), false);

        WalletApplication.localStore.setEncryptedPhrase(encryptedPhrase);
        WalletApplication.localStore.setPassphraseUsed(true);
        WalletApplication.localStore.setEncryptedPassphrase(null);

        // endregion

        encryptedPhrase = WalletApplication.localStore.getEncryptedPhrase();
        assertNotNull(encryptedPhrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        String decryptedPhrase = AccountUtils.Companion.getDecryptedString(encryptedPhrase, keyPair);
        String decryptedPassphrase = AccountUtils.Companion.getDecryptedPassphrase(WalletApplication.localStore.getEncryptedPassphrase(), keyPair);

        if (AccountUtils.Companion.isOldWalletWithPassphrase()) {
            Pair<String, String> decryptedPair = AccountUtils.Companion.getOldDecryptedPair(encryptedPhrase, keyPair.getPrivate());
            decryptedPhrase = decryptedPair.getFirst();
            decryptedPassphrase = decryptedPair.getSecond();
        }

        assertEquals(mnemonicString, decryptedPhrase);
        assertEquals(passphrase, decryptedPassphrase);
        WalletApplication.localStore.clearUserData();
    }

    // TODO: Remove in new app
    // Creates a passphrase wallet with the <= 1.0.3 version
    // ensures that the user can login and use the app on future versions
    @Test
    public void backwards_compatibility_test_recovery() {
        Context context = InstrumentationRegistry.getTargetContext();
        String mnemonicString =  String.join(" ", mnemonic);
        String passphrase = "this_is_a_passphrase";
        
        AccountUtils.Companion.encryptAndStoreWallet(InstrumentationRegistry.getTargetContext(),
                mnemonicString, passphrase, pin);
        String encryptedPhrase = WalletApplication.localStore.getEncryptedPhrase();
        assertNotNull(encryptedPhrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        String decryptedPhrase = AccountUtils.Companion.getDecryptedString(encryptedPhrase, keyPair);
        String decryptedPassphrase = AccountUtils.Companion.getDecryptedPassphrase(WalletApplication.localStore.getEncryptedPassphrase(), keyPair);

        assertEquals(mnemonicString, decryptedPhrase);
        assertEquals(passphrase, decryptedPassphrase);
    }
}
