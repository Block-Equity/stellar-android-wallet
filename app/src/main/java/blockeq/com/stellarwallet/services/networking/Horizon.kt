package blockeq.com.stellarwallet.services.networking

import android.os.AsyncTask
import android.util.Log
import blockeq.com.stellarwallet.interfaces.OnLoadAccount
import blockeq.com.stellarwallet.interfaces.OnLoadEffects
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.requests.RequestBuilder
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.Page
import org.stellar.sdk.responses.effects.EffectResponse
import java.util.ArrayList

class Horizon {
    companion object {
        const val PROD_SERVER = "https://horizon.stellar.org"
        const val TEST_SERVER = "https://horizon-testnet.stellar.org"

        private val TAG = Horizon::class.java.simpleName

        class LoadAccountTask(private val listener: OnLoadAccount) : AsyncTask<KeyPair, Void, AccountResponse>() {
            override fun doInBackground(vararg pair: KeyPair) : AccountResponse? {
                val server = Server(PROD_SERVER)
                var account : AccountResponse? = null
                try {
                    account = server.accounts().account(pair[0])

                } catch (error : ErrorResponse) {
                    Log.d(TAG, error.body.toString())
                }

                return account
            }

            override fun onPostExecute(result: AccountResponse?) {
                listener.onLoadAccount(result)
            }
        }

        class LoadEffectsTask(private val listener: OnLoadEffects) : AsyncTask<KeyPair, Void, ArrayList<EffectResponse>?>() {
            override fun doInBackground(vararg pair: KeyPair?): ArrayList<EffectResponse>? {
                val server = Server(PROD_SERVER)
                var effectResults : Page<EffectResponse>? = null
                try {
                    effectResults = server.effects().order(RequestBuilder.Order.DESC).forAccount(pair[0]).execute()
                } catch (error : ErrorResponse) {
                    Log.d(TAG, error.body.toString())
                }

                return effectResults?.records
            }

            override fun onPostExecute(result: ArrayList<EffectResponse>?) {
                listener.onLoadEffects(result)
            }

        }
    }
}
