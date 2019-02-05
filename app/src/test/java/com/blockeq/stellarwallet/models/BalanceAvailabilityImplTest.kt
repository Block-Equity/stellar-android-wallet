package com.blockeq.stellarwallet.models

import com.google.gson.reflect.TypeToken
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.stellar.sdk.responses.*
import java.io.File

class BalanceAvailabilityImplTest {
    /**
     * Native ASSET
     * (base amount 2) * 0.5
     * (default is one) additional signers * 1
     * trustlines 2 * 0.5
     * open offer * 0.5
     *
     * https://github.com/Block-Equity/stellar-ios-wallet/blob/ee2414061e309e2b97c4883541ca1cd335994487/StellarHub/Objects/StellarAccount.swift#L253
     * posted for trade X
     */
    @Test
    fun test_1(){
        val account : AccountResponse = getAccount(getFileFromPath(this,"account.json").readText())
        val offers : ArrayList<OfferResponse> = getOffers(getFileFromPath(this,"offer.json").readText())

        val availability = BalanceAvailabilityImpl(
                account, offers)

        val nativeAsset = availability.getNativeAssetAvailability()
        assertEquals(nativeAsset.baseAmount, 1f)
        assertEquals(nativeAsset.additionalSignersAmount, account.signers.size-1f)
        assertEquals(nativeAsset.trustLinesAmount, (account.balances.size-1)*0.5f)
        assertEquals(nativeAsset.openOffersAmount, offers.size * 0.5f)
        assertEquals(nativeAsset.postedForTradeAmount, 11.0f)
        assertEquals(nativeAsset.totalAvailable,
                nativeAsset.total
                - nativeAsset.baseAmount
                - nativeAsset.additionalSignersAmount
                - nativeAsset.postedForTradeAmount
                - nativeAsset.trustLinesAmount
                - nativeAsset.openOffersAmount)
    }

    @Test
    fun test_offer_json(){
        val file = getFileFromPath(this,"offer.json")
        assert(file.exists())
        val json = file.readText()
        assert(getOffers(json).size == 4)
    }

    @Test
    fun test_account_json(){
        val file = getFileFromPath(this,"account.json")
        assert(file.exists())
        val json = file.readText()
        assert(getAccount(json).balances.size == 3)
    }

    @Test
    fun test_balance_offer(){
        val file = getFileFromPath(this,"offer.json")
        assert(file.exists())
        val json = file.readText()
        assert(getOffers(json).size == 4)
    }

    private fun getOffers(json : String) : ArrayList<OfferResponse> {
        val type = object : TypeToken<Page<OfferResponse>>() { }
        val page: Page<OfferResponse> = GsonSingleton.getInstance().fromJson(json, type.type)
        return page.records
    }

    private fun getAccount(json : String) : AccountResponse {
        val type = object : TypeToken<AccountResponse>() { }
        return GsonSingleton.getInstance().fromJson(json, type.type)
    }

    private fun getBalances() : Array<AccountResponse.Balance> {
        val type = object : TypeToken<Array<AccountResponse.Balance>>() { }
        return GsonSingleton.getInstance().fromJson("json file", type.type)
    }

    private fun getFileFromPath(obj: Any, fileName: String): File {
        val classLoader = obj.javaClass.classLoader
        val resource = classLoader!!.getResource(fileName)
        return File(resource.path)
    }
}