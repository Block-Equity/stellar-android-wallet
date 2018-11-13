package blockeq.com.stellarwallet.utils;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyPair;

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
        String phrase = AccountUtils.Companion.getEncryptedMnemonicPhrase(InstrumentationRegistry.getTargetContext(), mnemonicString, null, pin);
        assertNotNull(phrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        Pair<String, String> decryptedPair = AccountUtils.Companion.getDecryptedMnemonicPhrasePair(phrase, keyPair.getPrivate());
        assertNull(decryptedPair.component2());
        assertEquals(decryptedPair.component1(), mnemonicString);
    }

    @Test
    public void basic_encryption_mnemonic_with_pass_phrase() {
        Context context = InstrumentationRegistry.getTargetContext();
        String mnemonicString =  String.join(" ", mnemonic);
        String passPhrase = "this_is_a_passphrase";

        String phrase = AccountUtils.Companion.getEncryptedMnemonicPhrase(InstrumentationRegistry.getTargetContext(), mnemonicString, passPhrase, pin);
        assertNotNull(phrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        Pair<String, String> decryptedPair = AccountUtils.Companion.getDecryptedMnemonicPhrasePair(phrase, keyPair.getPrivate());
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
    //        String phrase = AccountUtils.Companion.getEncryptedMnemonicPhrase(InstrumentationRegistry.getTargetContext(), mnemonicString, passPhrase, pin);
    //        assertNotNull(phrase);
    //
    //        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
    //        assertNotNull(keyPair);
    //
    //        Pair<String, String> decryptedPair = AccountUtils.Companion.getDecryptedMnemonicPhrasePair(phrase, keyPair.getPrivate());
    //        assertEquals(passPhrase, decryptedPair.component2());
    //        assertEquals(decryptedPair.component1(), mnemonicString);
    //    }
}