package blockeq.com.stellarwallet.helpers

import android.os.AsyncTask
import blockeq.com.stellarwallet.WalletApplication
import com.soneso.stellarmnemonics.Wallet


class StellarAddress {
    companion object {

        const val USER_INDEX = 0

        class Generate : AsyncTask<String, Void, Void>() {

            override fun doInBackground(vararg mnemonic: String): Void? {
                val keyPair = Wallet.createKeyPair(mnemonic[0].toCharArray(), null, USER_INDEX)
                WalletApplication.localStore!!.publicKey = keyPair.accountId
                return null
            }
        }
    }
}
