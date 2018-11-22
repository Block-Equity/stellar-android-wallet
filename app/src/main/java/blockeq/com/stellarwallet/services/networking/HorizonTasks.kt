package blockeq.com.stellarwallet.services.networking;

import android.os.AsyncTask
import blockeq.com.stellarwallet.interfaces.OnLoadAccount
import blockeq.com.stellarwallet.interfaces.OnLoadEffects
import blockeq.com.stellarwallet.interfaces.SuccessErrorCallback
import blockeq.com.stellarwallet.models.DataAsset
import blockeq.com.stellarwallet.models.HorizonException
import org.stellar.sdk.Asset
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse

interface HorizonTasks {
    fun getLoadAccountTask(listener: OnLoadAccount) : AsyncTask<Void, Void, AccountResponse>
    fun getLoadEffectsTask(listener: OnLoadEffects) : AsyncTask<Void, Void, ArrayList<EffectResponse>?>
    fun getSendTask(listener: SuccessErrorCallback, destAddress: String, secretSeed: CharArray, memo: String, amount : String) : AsyncTask<Void, Void, HorizonException>
    fun getJoinInflationDestination(listener: SuccessErrorCallback, secretSeed: CharArray, inflationDest : String) : AsyncTask<Void, Void, HorizonException>
    fun getChangeTrust(listener: SuccessErrorCallback, asset: Asset, removeTrust: Boolean, secretSeed: CharArray) : AsyncTask<Void, Void, HorizonException?>
    fun getCreateMarketOffer(listener: Horizon.OnMarketOfferListener, secretSeed: CharArray, sellingAsset: Asset, buyingAsset: Asset, amount : String, price : String)
    fun getOrderBook(listener: Horizon.OnOrderBookListener, buyingAsset: DataAsset, sellingAsset: DataAsset)
    fun getOffers(listener: Horizon.OnOffersListener)
}