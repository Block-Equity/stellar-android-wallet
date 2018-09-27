package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.adapters.AssetsRecyclerViewAdapter
import blockeq.com.stellarwallet.models.SupportedAsset
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.content_assets_activity.*


class AssetsActivity : BasePopupActivity() {

    private var adapter :AssetsRecyclerViewAdapter? = null
    private var assetsList: ArrayList<Any>? = ArrayList()

    override fun setContent(): Int {
        return R.layout.content_assets_activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        loadAssets()
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

    //endregion

    private fun loadAssets() {
        assetsList!!.addAll(WalletApplication.localStore!!.balances!!)
        assetsList!!.add("BlockEQ Assets")
        loadSupportedAssets()
    }

    private fun loadSupportedAssets() {
        val queue = Volley.newRequestQueue(this)
        val url = "https://api-staging.blockeq.com/directory/supportedAssets?asArray=true"

        // TODO: Use retrofit and dagger
        val request = JsonArrayRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    // display response
                    val gson = GsonBuilder().create()
                    val token = object : TypeToken<List<SupportedAsset>>(){}.type

                    val supportedAssetsList = gson.fromJson<List<SupportedAsset>>(response.toString(), token)
                    assetsList!!.addAll(supportedAssetsList)
                    adapter!!.notifyDataSetChanged()
                    Log.d("test", "test")
                },
                Response.ErrorListener {
                    Log.d("he", it.message)
                })

        queue.add(request)
    }

}
