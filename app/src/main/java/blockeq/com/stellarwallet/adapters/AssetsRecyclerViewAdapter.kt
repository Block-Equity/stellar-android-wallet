package blockeq.com.stellarwallet.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.utils.StringFormat.Companion.truncateDecimalPlaces
import org.stellar.sdk.responses.AccountResponse
import java.util.*

class AssetsRecyclerViewAdapter(var context: Context, var items : ArrayList<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_ASSET = 0
        const val TYPE_HEADER = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when(viewType) {
            TYPE_ASSET -> {
                val v = inflater.inflate(R.layout.item_asset, parent, false)
                AssetViewHolder(v)
            }
            else -> {
                val v = inflater.inflate(R.layout.item_asset_header, parent, false)
                AssetHeaderViewHolder(v)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            items[position] is AccountResponse.Balance -> TYPE_ASSET
            else -> TYPE_HEADER
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_ASSET -> {
                val vh = holder as AssetViewHolder
                configureAssetViewHolder(vh, position)
            }
            else -> {
                val vh = holder as AssetHeaderViewHolder
                configureAssetHeaderViewHolder(vh, position)
            }
        }
    }

    //region View Holders

    class AssetViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var assetImage : ImageView? = null
        var assetName : TextView? = null
        var assetAmount : TextView? = null
        var assetButton : Button? = null

        init {
            assetImage = v.findViewById(R.id.assetImageView)
            assetName = v.findViewById(R.id.assetNameTextView)
            assetAmount = v.findViewById(R.id.assetAmountTextView)
            assetButton = v.findViewById(R.id.assetButton)
        }
    }

    class AssetHeaderViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var title : TextView? = null

        init {
            title = v.findViewById(R.id.titleText)
        }
    }

    //endregion

    //region Bind View Holders

    private fun configureAssetViewHolder(viewHolder : AssetViewHolder, position : Int) {
        val asset = items[position] as AccountResponse.Balance

        if (asset.assetType == Constants.LUMENS_ASSET_TYPE) {
            viewHolder.assetName!!.text = "Stellar Lumens"
        } else {
            viewHolder.assetName!!.text = asset.assetCode
        }

        viewHolder.assetAmount!!.text = truncateDecimalPlaces(asset.balance)

    }

    private fun configureAssetHeaderViewHolder(viewHolder : AssetHeaderViewHolder, position : Int) {
        val titleText = items[position] as String
        viewHolder.title!!.text = titleText
    }

    //endregion
}
