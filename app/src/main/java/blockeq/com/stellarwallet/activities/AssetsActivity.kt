package blockeq.com.stellarwallet.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.adapters.AssetsRecyclerViewAdapter
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.interfaces.ChangeTrustlineListener
import blockeq.com.stellarwallet.interfaces.OnLoadAccount
import blockeq.com.stellarwallet.interfaces.SuccessErrorCallback
import blockeq.com.stellarwallet.models.SupportedAsset
import blockeq.com.stellarwallet.models.SupportedAssetType
import blockeq.com.stellarwallet.services.networking.Horizon
import blockeq.com.stellarwallet.utils.AccountUtils
import blockeq.com.stellarwallet.utils.NetworkUtils
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.content_assets_activity.*
import org.stellar.sdk.Asset
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse


class AssetsActivity : BasePopupActivity(), ChangeTrustlineListener {

    private var adapter :AssetsRecyclerViewAdapter? = null
    private var assetsList: ArrayList<Any>? = ArrayList()
    private var map: Map<String, SupportedAsset>? = null
    private lateinit var context : Context

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
    }
    //endregion

    //region User Interface

    private fun bindAdapter() {
        adapter = AssetsRecyclerViewAdapter(this, this, assetsList!!)
        assetsRecyclerView.adapter = adapter
        assetsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun updateAdapter() {
        assetsList!!.clear()
        assetsList!!.addAll(convertBalanceToSupportedAsset(WalletApplication.localStore.balances!!, map!!))
        val filteredList = getFilteredSupportedAssets(map!!)
        if (!filteredList.isEmpty()) {
            assetsList!!.add(getString(R.string.supported_assets_header))
            assetsList!!.addAll(filteredList)
        }

        adapter!!.notifyDataSetChanged()
        progressBar.visibility = View.GONE
    }

    //endregion

    private fun convertBalanceToSupportedAsset(balances: Array<AccountResponse.Balance>,
                                               supportedAssetsMap: Map<String, SupportedAsset>) : List<SupportedAsset> {

        val lumenSupportedAsset = SupportedAsset(0, Constants.LUMENS_ASSET_CODE, Constants.LUMENS_IMAGE_URL,
                "", "", Constants.LUMENS_ASSET_NAME, "", "",
                "0", SupportedAssetType.ADDED, null)

        val list = ArrayList<SupportedAsset>()
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
                       asset.code = asset.code
                       asset.asset = it.asset
                       return@map asset
                   }
                   else -> return@map null
               }
           }

           // This cast is guaranteed to succeed
           @Suppress("UNCHECKED_CAST")
           list.addAll((nullableAssets.filter { it != null }) as List<SupportedAsset>)
       }

        return list
    }

    private fun getFilteredSupportedAssets(map: Map<String, SupportedAsset>): List<SupportedAsset> {
        return map.values.filter { it ->
            it.code.toUpperCase() !in WalletApplication.localStore.balances!!.map { it.assetCode }
        }
    }

    private fun loadSupportedAssets() {
        val queue = Volley.newRequestQueue(this)

        // TODO: Use retrofit and dagger
        val request = JsonObjectRequest(Request.Method.GET, Constants.BLOCKEQ_BASE_URL, null,
                Response.Listener { response ->
                    // display response
                    val gson = GsonBuilder().create()
                    val token = object : TypeToken<Map<String, SupportedAsset>>(){}.type

                    map = gson.fromJson<Map<String, SupportedAsset>>(response.toString(), token)
                    updateAdapter()
                },
                Response.ErrorListener {
                    Toast.makeText(this, getString(R.string.error_supported_assets_message), Toast.LENGTH_SHORT).show()
                })

        queue.add(request)
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

                override fun onError() {
                    Toast.makeText(this@AssetsActivity, getString(R.string.error_trustline_changed), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@AssetsActivity, getString(R.string.error_supported_assets_message), Toast.LENGTH_SHORT).show()
                }
            }).execute()
        }
    }
    //endregion
}
