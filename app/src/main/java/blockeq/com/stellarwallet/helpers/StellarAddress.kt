package blockeq.com.stellarwallet.helpers

import android.os.AsyncTask
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.interfaces.OnWalletSeedCreated
import blockeq.com.stellarwallet.models.Session
import com.soneso.stellarmnemonics.Wallet
import org.stellar.sdk.KeyPair


class StellarAddress {
    companion object {

        const val USER_INDEX = 0

        class GenerateStellarAddressTask(private val listener: OnWalletSeedCreated) : AsyncTask<String, Void, KeyPair>() {

            override fun doInBackground(vararg mnemonic: String) : KeyPair? {
                val keyPair = Wallet.createKeyPair(mnemonic[0].toCharArray(), null, USER_INDEX)
                WalletApplication.localStore!!.publicKey = keyPair.accountId
                WalletApplication.session = Session(keyPair)
                return keyPair
            }

            override fun onPostExecute(keyPair: KeyPair?) {
                listener.onWalletSeedCreated(keyPair)
            }
        }
    }
}