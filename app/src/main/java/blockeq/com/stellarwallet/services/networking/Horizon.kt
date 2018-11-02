package blockeq.com.stellarwallet.services.networking

import android.os.AsyncTask
import android.util.Log
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.interfaces.OnLoadAccount
import blockeq.com.stellarwallet.interfaces.OnLoadEffects
import blockeq.com.stellarwallet.interfaces.SuccessErrorCallback
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import org.stellar.sdk.*
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.requests.RequestBuilder
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.Page
import org.stellar.sdk.responses.effects.EffectResponse

object Horizon : HorizonTasks {
    private const val PROD_SERVER = "https://horizon.stellar.org"
    private const val TEST_SERVER = "https://horizon-testnet.stellar.org"
    private const val SERVER_ERROR_MESSAGE = "Error response from the server."

    private val TAG = Horizon::class.java.simpleName

    override fun getLoadEffectsTask(listener: OnLoadEffects): AsyncTask<Void, Void, ArrayList<EffectResponse>?> {
        return LoadEffectsTask(listener)
    }

    override fun getSendTask(listener: SuccessErrorCallback, destAddress: String, secretSeed: CharArray, memo: String, amount: String): AsyncTask<Void, Void, Exception> {
        return SendTask(listener, destAddress, secretSeed, memo, amount)
    }

    override fun getJoinInflationDestination(listener: SuccessErrorCallback, secretSeed: CharArray, inflationDest: String): AsyncTask<Void, Void, Exception> {
        return JoinInflationDestination(listener, secretSeed, inflationDest)
    }

    override fun getChangeTrust(listener: SuccessErrorCallback, asset: Asset, removeTrust: Boolean, secretSeed: CharArray): AsyncTask<Void, Void, Exception> {
        return ChangeTrust(listener, asset, removeTrust, secretSeed)
    }

    override fun getLoadAccountTask(listener: OnLoadAccount): AsyncTask<Void, Void, AccountResponse> {
        return LoadAccountTask(listener)
    }

    private class LoadAccountTask(private val listener: OnLoadAccount) : AsyncTask<Void, Void, AccountResponse>() {
        override fun doInBackground(vararg params: Void?) : AccountResponse? {
            val server = getServer()
            val sourceKeyPair = KeyPair.fromAccountId(WalletApplication.localStore.stellarAccountId)
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

    private class LoadEffectsTask(private val listener: OnLoadEffects) : AsyncTask<Void, Void, ArrayList<EffectResponse>?>() {
        override fun doInBackground(vararg params: Void?): ArrayList<EffectResponse>? {
            val server = getServer()
            val sourceKeyPair = KeyPair.fromAccountId(WalletApplication.localStore.stellarAccountId)
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

    private class SendTask(private val listener: SuccessErrorCallback, private val destAddress: String,
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

    private class JoinInflationDestination(private val listener: SuccessErrorCallback,
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

    private class ChangeTrust(private val listener: SuccessErrorCallback, private val asset: Asset,
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

    interface OnMarketOfferListener {
      fun onExecuted()
      fun onFailed()
    }

    override fun getCreateMarketOffer(listener: OnMarketOfferListener, secretSeed: CharArray, sellingAsset: Asset, buyingAsset: Asset, amount: String, price: String) {
        AsyncTask.execute {
            val server = getServer()
            val managedOfferOperation = ManageOfferOperation.Builder(sellingAsset, buyingAsset, amount, price).build()
            val sourceKeyPair = KeyPair.fromSecretSeed(secretSeed)
            val sourceAccount = server.accounts().account(sourceKeyPair)

            val transaction = Transaction.Builder(sourceAccount).addOperation(managedOfferOperation).build()
            transaction.sign(sourceKeyPair)
            val response = server.submitTransaction(transaction)
            if (response.isSuccess) {
                listener.onFailed()
            } else {
                listener.onExecuted()
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
        val clientOkHttp = OkHttpClient.Builder()
                .addNetworkInterceptor(StethoInterceptor())
                .build()
        val server = Server(PROD_SERVER)
        server.httpClient = clientOkHttp

        return server
    }
}
