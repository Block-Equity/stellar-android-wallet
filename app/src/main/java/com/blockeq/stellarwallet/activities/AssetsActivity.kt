package com.blockeq.stellarwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.adapters.AssetsRecyclerViewAdapter
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.interfaces.ChangeTrustlineListener
import com.blockeq.stellarwallet.interfaces.OnLoadAccount
import com.blockeq.stellarwallet.interfaces.SuccessErrorCallback
import com.blockeq.stellarwallet.models.HorizonException
import com.blockeq.stellarwallet.responses.SupportedAssetResponse
import com.blockeq.stellarwallet.responses.SupportedAssetType
import com.blockeq.stellarwallet.networking.Horizon
import com.blockeq.stellarwallet.networking.RetrofitClient
import com.blockeq.stellarwallet.networking.api.SupportedAssetAPI
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.NetworkUtils
import kotlinx.android.synthetic.main.content_assets_activity.*
import org.stellar.sdk.Asset
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse
import retrofit2.Call
import retrofit2.Callback

class AssetsActivity : BasePopupActivity(), ChangeTrustlineListener {

    private var map: Map<String, SupportedAssetResponse>? = null
    private var assetsList: ArrayList<Any> = ArrayList()
    private lateinit var context : Context
    private lateinit var adapter : AssetsRecyclerViewAdapter

    override fun setContent(): Int {
        return R.layout.content_assets_activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        loadSupportedAssets()
        context = applicationContext
    }

    private fun setupUI() {
        progressBar.visibility = View.VISIBLE
        bindAdapter()
        titleText.text = getString(R.string.asset_title_text)
        manuallyAddAssetButton.setOnClickListener {
            startActivity(Intent(this@AssetsActivity, AddAssetActivity::class.java))
        }
    }
    //endregion

    //region User Interface

    private fun bindAdapter() {
        adapter = AssetsRecyclerViewAdapter(this, this, assetsList)
        assetsRecyclerView.adapter = adapter
        assetsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun updateAdapter() {
        assetsList.clear()
        assetsList.addAll(convertBalanceToSupportedAsset(WalletApplication.localStore.balances!!, map!!))
        val filteredList = getFilteredSupportedAssets(map!!)
        if (!filteredList.isEmpty()) {
            assetsList.add(getString(R.string.supported_assets_header))
            assetsList.addAll(filteredList)
        }

        adapter.notifyDataSetChanged()
        progressBar.visibility = View.GONE
    }

    //endregion

    private fun convertBalanceToSupportedAsset(balances: Array<AccountResponse.Balance>,
                                               supportedAssetsMap: Map<String, SupportedAssetResponse>) : List<SupportedAssetResponse> {

        val lumenSupportedAsset = SupportedAssetResponse(0, Constants.LUMENS_ASSET_CODE, Constants.LUMENS_IMAGE_URL,
                "", "", Constants.LUMENS_ASSET_NAME, "", "",
                "0", SupportedAssetType.ADDED, null)

        val list = ArrayList<SupportedAssetResponse>()
        list.add(lumenSupportedAsset)

       if (balances.isNotEmpty()) {
           val nullableAssets = balances.map {
               when {
                   it.assetType == Constants.LUMENS_ASSET_TYPE -> {
                       list[0].amount = it.balance
                       return@map null
                   }
                   supportedAssetsMap.containsKey(it.assetCode.toLowerCase()) -> {
                       val asset = supportedAssetsMap[it.assetCode.toLowerCase()]!!
                       asset.amount = it.balance
                       asset.type = SupportedAssetType.ADDED
                       asset.asset = it.asset
                       return@map asset
                   }
                   else -> {
                       val asset = SupportedAssetResponse(0, it.assetCode.toLowerCase(), "",
                               it.assetIssuer.accountId, it.limit, it.assetCode, "",
                               "", it.balance, SupportedAssetType.ADDED, it.asset)
                       return@map asset
                   }
               }
           }

           // This cast is guaranteed to succeed
           @Suppress("UNCHECKED_CAST")
           list.addAll((nullableAssets.filter { it != null }) as List<SupportedAssetResponse>)
       }

        return list
    }

    private fun getFilteredSupportedAssets(map: Map<String, SupportedAssetResponse>): List<SupportedAssetResponse> {
        return map.values.filter { it ->
            it.code.toUpperCase() !in WalletApplication.localStore.balances!!.map { it.assetCode }
        }
    }

    private fun loadSupportedAssets() {
        val retrofitClient = RetrofitClient.getRetrofitClient(Constants.BLOCKEQ_BASE_URL)
        val service = retrofitClient.create(SupportedAssetAPI::class.java)

        val call = service.requestSupportedAssets()

        call.enqueue(object : Callback<Map<String, SupportedAssetResponse>>{
            override fun onFailure(call: Call<Map<String, SupportedAssetResponse>>, t: Throwable) {
                Toast.makeText(this@AssetsActivity, getString(R.string.error_supported_assets_message), Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Map<String, SupportedAssetResponse>>, response: retrofit2.Response<Map<String, SupportedAssetResponse>>) {
               map = response.body()
               updateAdapter()
            }

        })
    }

    //region Call backs

    override fun changeTrustline(asset: Asset, isRemoveAsset: Boolean) {
        progressBar.visibility = View.VISIBLE
        val secretSeed = AccountUtils.getSecretSeed(context)
        changeTrustLine(secretSeed, asset, isRemoveAsset)
    }

    private fun changeTrustLine(secretSeed: CharArray, assetToChange: Asset, isRemove: Boolean) {
        if (NetworkUtils(this).isNetworkAvailable()) {
            Horizon.getChangeTrust(object : SuccessErrorCallback {
                override fun onSuccess() {
                    reloadDataForAdapter()
                    Toast.makeText(this@AssetsActivity, getString(R.string.success_trustline_changed), Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    if (isRemove) {
                        WalletApplication.userSession.currAssetCode = Constants.LUMENS_ASSET_TYPE
                        WalletApplication.userSession.currAssetName = Constants.LUMENS_ASSET_NAME
                        WalletApplication.userSession.currAssetIssuer = ""
                        finish()
                    }
                }

                override fun onError(error: HorizonException) {
                    Toast.makeText(this@AssetsActivity, error.message(this@AssetsActivity), Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }, assetToChange, isRemove, secretSeed).execute()
        } else {
            NetworkUtils(this).displayNoNetwork()
            progressBar.visibility = View.GONE
        }
    }

    fun reloadDataForAdapter() {
        if (NetworkUtils(this).isNetworkAvailable()) {
            Horizon.getLoadAccountTask(object: OnLoadAccount {

                override fun onLoadAccount(result: AccountResponse?) {
                    if (result != null) {
                        WalletApplication.localStore.balances = result.balances
                        updateAdapter()
                    }
                }

                override fun onError(error: ErrorResponse) {
                    Toast.makeText(context, getString(R.string.error_supported_assets_message), Toast.LENGTH_SHORT).show()
                }
            }).execute()
        }
    }
    //endregion
}
