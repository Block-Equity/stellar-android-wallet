package com.blockeq.stellarwallet.remote

import android.os.AsyncTask
import com.blockeq.stellarwallet.interfaces.OnLoadAccount
import com.blockeq.stellarwallet.mvvm.effects.remote.OnLoadEffects
import com.blockeq.stellarwallet.interfaces.SuccessErrorCallback
import com.blockeq.stellarwallet.models.DataAsset
import com.blockeq.stellarwallet.models.HorizonException
import org.stellar.sdk.Asset
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.requests.SSEStream
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse

interface HorizonTasks {
    fun init(server: ServerType)
    fun registerForEffects(cursor: String, listener: EventListener<EffectResponse>): SSEStream<EffectResponse>?
    fun getLoadAccountTask(listener: OnLoadAccount) : AsyncTask<Void, Void, AccountResponse>
    fun getLoadEffectsTask(cursor: String, limit: Int, listener: OnLoadEffects) : AsyncTask<Void, Void, ArrayList<EffectResponse>?>
    fun getSendTask(listener: SuccessErrorCallback, destAddress: String, secretSeed: CharArray, memo: String, amount : String) : AsyncTask<Void, Void, HorizonException>
    fun getJoinInflationDestination(listener: SuccessErrorCallback, secretSeed: CharArray, inflationDest : String) : AsyncTask<Void, Void, HorizonException>
    fun getChangeTrust(listener: SuccessErrorCallback, asset: Asset, removeTrust: Boolean, secretSeed: CharArray) : AsyncTask<Void, Void, HorizonException?>
    fun getCreateMarketOffer(listener: Horizon.OnMarketOfferListener, secretSeed: CharArray, sellingAsset: Asset, buyingAsset: Asset, amount : String, price : String)
    fun getOrderBook(listener: Horizon.OnOrderBookListener, buyingAsset: DataAsset, sellingAsset: DataAsset)
    fun getOffers(listener: Horizon.OnOffersListener)
    fun deleteOffer(id:Long, secretSeed : CharArray, selling: Asset, buying: Asset, price: String, listener: Horizon.OnMarketOfferListener)
}