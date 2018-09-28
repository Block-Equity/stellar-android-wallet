package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.adapters.AssetsRecyclerViewAdapter
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.models.SupportedAsset
import blockeq.com.stellarwallet.models.SupportedAssetType
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.content_assets_activity.*
import org.stellar.sdk.responses.AccountResponse


class AssetsActivity : BasePopupActivity() {

    private var adapter :AssetsRecyclerViewAdapter? = null
    private var assetsList: ArrayList<Any>? = ArrayList()

    override fun setContent(): Int {
        return R.layout.content_assets_activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        loadSupportedAssets()
    }

    override fun setupUI() {
        bindAdapter()
        titleText.text = getString(R.string.asset_title_text)
    }
    //endregion

    //region User Interface

    private fun bindAdapter() {
        adapter = AssetsRecyclerViewAdapter(this, assetsList!!)
        assetsRecyclerView.adapter = adapter
        assetsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun updateAdapter(map: Map<String, SupportedAsset>) {
        assetsList!!.addAll(convertBalanceToSupportedAsset(WalletApplication.localStore!!.balances!!, map))
        assetsList!!.add(getString(R.string.supported_assets_header))
        assetsList!!.addAll(getFilteredSupportedAssets(map))
        adapter!!.notifyDataSetChanged()
    }

    //endregion

    private fun convertBalanceToSupportedAsset(balances: Array<AccountResponse.Balance>,
                                               supportedAssetsMap: Map<String, SupportedAsset>) : List<SupportedAsset> {

        val lumenSupportedAsset = SupportedAsset(0, Constants.LUMENS_ASSET_CODE, Constants.LUMENS_IMAGE_URL,
                "", "", Constants.LUMENS_ASSET_NAME, "", "",
                "0", SupportedAssetType.ADDED)

        val list = ArrayList<SupportedAsset>()
        list.add(lumenSupportedAsset)

       if (balances.isNotEmpty()) {
           val nullableAssets = balances.map {
               if (it.assetType == Constants.LUMENS_ASSET_TYPE) {
                   list[0].amount = it.balance
                   return@map null
               } else if (supportedAssetsMap.containsKey(it.assetCode.toLowerCase())){
                   val asset = supportedAssetsMap[it.assetCode.toLowerCase()]!!
                   asset.amount = it.balance
                   asset.type = SupportedAssetType.ADDED
                   asset.code = asset.code
                   return@map asset
               } else {
                   return@map null
               }
           }

           // This cast is guaranteed to succeed
           list.addAll((nullableAssets.filter { it != null }) as List<SupportedAsset>)
       }

        return list
    }

    private fun getFilteredSupportedAssets(map: Map<String, SupportedAsset>): List<SupportedAsset> {
        return map.values.filter {
            it.code.toUpperCase() !in WalletApplication.localStore!!.balances!!.map { it.assetCode }
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

                    val supportedAssetsList = gson.fromJson<Map<String, SupportedAsset>>(response.toString(), token)
                    updateAdapter(supportedAssetsList)
                },
                Response.ErrorListener {
                    Toast.makeText(this, getString(R.string.error_supported_assets_message), Toast.LENGTH_SHORT).show()
                })

        queue.add(request)
    }

}
