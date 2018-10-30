package blockeq.com.stellarwallet.services.networking

import android.os.AsyncTask
import android.util.Log
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.interfaces.OnLoadAccount
import blockeq.com.stellarwallet.interfaces.OnLoadEffects
import blockeq.com.stellarwallet.interfaces.SuccessErrorCallback
import org.stellar.sdk.ChangeTrustOperation
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Memo
import org.stellar.sdk.Network
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.PaymentOperation
import org.stellar.sdk.Server
import org.stellar.sdk.SetOptionsOperation
import org.stellar.sdk.Asset
import org.stellar.sdk.CreateAccountOperation
import org.stellar.sdk.Transaction
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.requests.RequestBuilder
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.Page
import org.stellar.sdk.responses.effects.EffectResponse


class Horizon {
    companion object {
        const val PROD_SERVER = "https://horizon.stellar.org"
        const val TEST_SERVER = "https://horizon-testnet.stellar.org"
        const val SERVER_ERROR_MESSAGE = "Error response from the server."

        private val TAG = Horizon::class.java.simpleName

        class LoadAccountTask(private val listener: OnLoadAccount) : AsyncTask<Void, Void, AccountResponse>() {
            override fun doInBackground(vararg params: Void?) : AccountResponse? {
                val server = getServer()
                val sourceKeyPair = KeyPair.fromAccountId(WalletApplication.localStore.publicKey)
                var account : AccountResponse? = null
                try {
                    account = server.accounts().account(sourceKeyPair)

                } catch (error : Exception) {
                    Log.d(TAG, error.message.toString())
                    if (error is ErrorResponse) {
                        listener.onError(error)
                    } else {
                        listener.onError(ErrorResponse(Constants.UNKNOWN_ERROR, error.message))
                    }
                }

                return account
            }

            override fun onPostExecute(result: AccountResponse?) {
                listener.onLoadAccount(result)
            }
        }

        class LoadEffectsTask(private val listener: OnLoadEffects) : AsyncTask<Void, Void, ArrayList<EffectResponse>?>() {
            override fun doInBackground(vararg params: Void?): ArrayList<EffectResponse>? {
                val server = getServer()
                val sourceKeyPair = KeyPair.fromAccountId(WalletApplication.localStore.publicKey)
                var effectResults : Page<EffectResponse>? = null
                try {
                    effectResults = server.effects().order(RequestBuilder.Order.DESC)
                            .limit(Constants.NUM_TRANSACTIONS_SHOWN)
                            .forAccount(sourceKeyPair).execute()
                } catch (error : Exception) {
                    Log.d(TAG, error.message.toString())
                }

                return effectResults?.records
            }

            override fun onPostExecute(result: ArrayList<EffectResponse>?) {
                listener.onLoadEffects(result)
            }

        }

        class SendTask(private val listener: SuccessErrorCallback, private val destAddress: String,
                       private val secretSeed: CharArray, private val memo: String,
                       private val amount : String) : AsyncTask<Void, Void, Exception>() {

            override fun doInBackground(vararg params: Void?): Exception? {
                val server = getServer()
                val sourceKeyPair = KeyPair.fromSecretSeed(secretSeed)
                val destKeyPair = KeyPair.fromAccountId(destAddress)
                var isCreateAccount = false

                Network.usePublicNetwork()

                try {
                    try {
                        server.accounts().account(destKeyPair)
                    } catch (error : Exception) {
                        Log.d(TAG, error.message.toString())
                        if (error.message == SERVER_ERROR_MESSAGE) {
                            isCreateAccount = true
                        } else {
                            return error
                        }
                    }

                    val sourceAccount = server.accounts().account(sourceKeyPair)

                    val transaction = if (isCreateAccount) {
                        Transaction.Builder(sourceAccount)
                                .addOperation(CreateAccountOperation.Builder(destKeyPair, amount).build())
                                // A memo allows you to add your own metadata to a transaction. It's
                                // optional and does not affect how Stellar treats the transaction.
                                .addMemo(Memo.text(memo))
                                .build()
                    } else {
                        Transaction.Builder(sourceAccount)
                                .addOperation(PaymentOperation.Builder(destKeyPair, getCurrentAsset(), amount).build())
                                .addMemo(Memo.text(memo))
                                .build()
                    }

                    transaction.sign(sourceKeyPair)

                    server.submitTransaction(transaction)

                } catch (error : ErrorResponse) {
                    Log.d(TAG, error.body.toString())
                    return error
                }

                return null
            }

            override fun onPostExecute(result: Exception?) {
                if (result != null) {
                    listener.onError()
                } else {
                    listener.onSuccess()
                }
            }
        }

        class JoinInflationDestination(private val listener: SuccessErrorCallback,
                                       private val secretSeed: CharArray,
                                       private val inflationDest : String)
            : AsyncTask<Void, Void, Exception>() {

            override fun doInBackground(vararg params: Void?): Exception? {
                Network.usePublicNetwork()

                val server = getServer()
                val sourceKeyPair = KeyPair.fromSecretSeed(secretSeed)
                val destKeyPair = KeyPair.fromAccountId(inflationDest)

                try {
                    val sourceAccount = server.accounts().account(sourceKeyPair)

                    val transaction = Transaction.Builder(sourceAccount)
                            .addOperation(SetOptionsOperation.Builder()
                                    .setInflationDestination(destKeyPair)
                                    .build())
                            .build()

                    transaction.sign(sourceKeyPair)
                    server.submitTransaction(transaction)

                } catch (error : Exception) {
                    Log.d(TAG, error.message.toString())
                    return error
                }
                return null
            }

            override fun onPostExecute(result: Exception?) {
                if (result != null) {
                    listener.onError()
                } else {
                    listener.onSuccess()
                }
            }
        }

        class ChangeTrust(private val listener: SuccessErrorCallback, private val asset: Asset,
                          private val removeTrust: Boolean, private val secretSeed: CharArray)
            : AsyncTask<Void, Void, Exception>() {

            override fun doInBackground(vararg params: Void?): Exception? {
                Network.usePublicNetwork()

                val server = getServer()
                val sourceKeyPair = KeyPair.fromSecretSeed(secretSeed)
                val limit = if (removeTrust) "0.0000000" else Constants.MAX_ASSET_STRING_VALUE

                try {
                    val sourceAccount = server.accounts().account(sourceKeyPair)

                    val transaction = Transaction.Builder(sourceAccount)
                            .addOperation(ChangeTrustOperation.Builder(asset, limit).build())
                            .build()

                    transaction.sign(sourceKeyPair)
                    val response = server.submitTransaction(transaction)

                    if (!response.isSuccess) {
                        return Exception()
                    }

                } catch (error : ErrorResponse) {
                    Log.d(TAG, error.body.toString())
                    return error
                }
                return null
            }

            override fun onPostExecute(result: Exception?) {
                if (result != null) {
                    listener.onError()
                } else {
                    listener.onSuccess()
                }
            }
        }

        private fun getCurrentAsset(): Asset {
            val assetCode = WalletApplication.userSession.currAssetCode
            val assetIssuer = WalletApplication.userSession.currAssetIssuer

            return if (assetCode == Constants.LUMENS_ASSET_TYPE) {
                AssetTypeNative()
            } else {
                Asset.createNonNativeAsset(assetCode, KeyPair.fromAccountId(assetIssuer))
            }
        }

        private fun getServer() : Server {
            return Server(PROD_SERVER)
        }
    }
}
