package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.adapters.AssetsRecyclerViewAdapter
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
    }

    override fun setupUI() {
        titleText.text = getString(R.string.asset_title_text)
        bindAdapter()
    }
    //endregion

    //region User Interface

    private fun bindAdapter() {
        loadAssets()
        adapter = AssetsRecyclerViewAdapter(this, assetsList!!)
        assetsRecyclerView.adapter = adapter
        assetsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    //endregion

    private fun loadAssets() {
        assetsList!!.addAll(WalletApplication.localStore!!.balances!!)
        assetsList!!.add("BlockEQ Assets")
    }

}
