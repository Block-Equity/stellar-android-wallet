package blockeq.com.stellarwallet.services.networking

import android.os.AsyncTask
import android.util.Log
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.interfaces.OnLoadAccount
import blockeq.com.stellarwallet.interfaces.OnLoadEffects
import blockeq.com.stellarwallet.interfaces.OnSendPayment
import blockeq.com.stellarwallet.utils.StringFormat
import org.stellar.sdk.*
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.requests.RequestBuilder
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.Page
import org.stellar.sdk.responses.effects.EffectResponse
import java.util.*


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

        class SendTask(private val listener: OnSendPayment, private val destAddress: String,
                       private val memo: String, private val amount : String) : AsyncTask<KeyPair, Void, Void>() {

            override fun doInBackground(vararg params: KeyPair?): Void? {
                val server = Server(PROD_SERVER)
                val sourceKeyPair = params[0]!!
                val destKeyPair = KeyPair.fromAccountId(destAddress)

                try {
                    // Check if destination account exists
                    server.accounts().account(destKeyPair)
                } catch (error : ErrorResponse) {
                    Log.d(TAG, error.body.toString())
                    listener.onSendError()
                }

                try {
                    val sourceAccount = server.accounts().account(sourceKeyPair)

                    val transaction = Transaction.Builder(sourceAccount)
                            .addOperation(PaymentOperation.Builder(destKeyPair, AssetTypeNative(), amount).build())
                            // A memo allows you to add your own metadata to a transaction. It's
                            // optional and does not affect how Stellar treats the transaction.
                            .addMemo(Memo.text(memo))
                            .build()
                    transaction.sign(sourceKeyPair)

                    try {
                        val response = server.submitTransaction(transaction)

                        if (response.isSuccess) {
                            listener.OnSendSuccess()
                        } else {
                            listener.onSendError()
                        }
                    } catch (e: Exception) {
                        listener.onSendError()
                        // If the result is unknown (no response body, timeout etc.) we simply resubmit
                        // already built transaction:
                        // SubmitTransactionResponse response = server.submitTransaction(transaction);
                    }


                } catch (error : ErrorResponse) {
                    Log.d(TAG, error.body.toString())
                    listener.onSendError()
                }

                return null
            }
        }

        fun getBalance() : String {
            WalletApplication.localStore!!.balances?.forEach {
                if (it.assetType == Constants.LUMENS_ASSET_TYPE) {
                    //TODO: When switching assets, get the right balance for asset
                    return StringFormat.truncateDecimalPlaces(it.balance)
                }
            }
            return Constants.DEFAULT_ACCOUNT_BALANCE
        }
    }
}
