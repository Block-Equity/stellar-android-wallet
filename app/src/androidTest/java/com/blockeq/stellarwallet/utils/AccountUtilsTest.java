package com.blockeq.stellarwallet.utils;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.blockeq.stellarwallet.WalletApplication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyPair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class AccountUtilsTest {
    private String mnemonic12;
    private String mnemonic24;
    private Context context;
    private String pin = "1234";

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
        String phrase = WalletApplication.wallet.getEncryptedPhrase();

        assertNotNull(phrase);
    }

    @Test
    public void basic_encryption_mnemonic_with_passphrase() {
        String passPhrase = "this_is_a_passphrase";
        AccountUtils.Companion.encryptAndStoreWallet(context, mnemonic12, passPhrase, pin);
        String phrase = WalletApplication.wallet.getEncryptedPhrase();
        assertNotNull(phrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        String decryptedPhrase = AccountUtils.Companion.getDecryptedString(phrase, keyPair);
        String encryptedPassphrase = WalletApplication.wallet.getEncryptedPassphrase();
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
        String phrase = WalletApplication.wallet.getEncryptedPhrase();
        assertNotNull(phrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        String decryptedPhrase = AccountUtils.Companion.getDecryptedString(phrase, keyPair);
        String encryptedPassphrase = WalletApplication.wallet.getEncryptedPassphrase();
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
        String phrase = WalletApplication.wallet.getEncryptedPhrase();
        assertNotNull(phrase);

        KeyPair keyPair = AccountUtils.Companion.getPinMasterKey(context, pin);
        assertNotNull(keyPair);

        String decryptedPhrase = AccountUtils.Companion.getDecryptedString(phrase, keyPair);
        String encryptedPassphrase = WalletApplication.wallet.getEncryptedPassphrase();
        String decryptedPassphrase = null;
        if (encryptedPassphrase != null) {
            decryptedPassphrase = AccountUtils.Companion.getDecryptedString(encryptedPassphrase, keyPair);
        }

        assertEquals(mnemonic24, decryptedPhrase);
        assertEquals(passphrase, decryptedPassphrase);
    }

    @After
    public void cleanUp() {
        if (!GlobalGraphHelper.Companion.wipe(context)) {
            throw new IllegalStateException("failed to wipe");
        }
    }
}
