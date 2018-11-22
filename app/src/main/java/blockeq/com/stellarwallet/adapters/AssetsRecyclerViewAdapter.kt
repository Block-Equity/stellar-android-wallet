package blockeq.com.stellarwallet.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.activities.InflationActivity
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.interfaces.ChangeTrustlineListener
import blockeq.com.stellarwallet.models.SupportedAsset
import blockeq.com.stellarwallet.models.SupportedAssetType
import blockeq.com.stellarwallet.utils.AccountUtils
import blockeq.com.stellarwallet.utils.StringFormat
import com.squareup.picasso.Picasso
import org.stellar.sdk.Asset
import org.stellar.sdk.KeyPair

class AssetsRecyclerViewAdapter(var context: Context, private var listener: ChangeTrustlineListener, private var items : ArrayList<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TYPE_ASSET = 0
        const val TYPE_HEADER = 1
        const val TYPE_SUPPORTED_ASSET = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when(viewType) {
            TYPE_ASSET -> {
                val v = inflater.inflate(R.layout.item_asset, parent, false)
                AssetViewHolder(v)
            }
            TYPE_SUPPORTED_ASSET -> {
                val v = inflater.inflate(R.layout.item_asset, parent, false)
                SupportedAssetViewHolder(v)
            }
            else -> {
                val v = inflater.inflate(R.layout.item_asset_header, parent, false)
                AssetHeaderViewHolder(v)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            items[position] is SupportedAsset && (items[position] as SupportedAsset).type == SupportedAssetType.ADDED -> TYPE_ASSET
            items[position] is SupportedAsset -> TYPE_SUPPORTED_ASSET
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
            TYPE_SUPPORTED_ASSET -> {
                val vh = holder as SupportedAssetViewHolder
                configureSupportedAssetViewHolder(vh, position)
            }
            else -> {
                val vh = holder as AssetHeaderViewHolder
                configureAssetHeaderViewHolder(vh, position)
            }
        }
    }

    //region View Holders

    class AssetViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        val assetImage : ImageView = v.findViewById(R.id.assetImageView)
        val assetName : TextView = v.findViewById(R.id.assetNameTextView)
        val assetAmount : TextView = v.findViewById(R.id.assetAmountTextView)
        val assetButton : Button = v.findViewById(R.id.assetButton)
    }

    class AssetHeaderViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        val title : TextView = v.findViewById(R.id.titleText)
    }

    class SupportedAssetViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        val assetImage : ImageView = v.findViewById(R.id.assetImageView)
        val assetName : TextView = v.findViewById(R.id.assetNameTextView)
        val assetAmount : TextView = v.findViewById(R.id.assetAmountTextView)
        val assetButton : Button = v.findViewById(R.id.assetButton)
    }

    //endregion

    //region Bind View Holders

    private fun configureAssetViewHolder(viewHolder : AssetViewHolder, position : Int) {
        val asset = items[position] as SupportedAsset

        viewHolder.assetButton.visibility = View.VISIBLE
        viewHolder.assetName.text = asset.name
        viewHolder.assetAmount.text = String.format(context.getString(R.string.balance_template),
                StringFormat.truncateDecimalPlaces(asset.amount), asset.code.toUpperCase())

        Picasso.get().load(asset.image).into(viewHolder.assetImage)

        if (asset.code == Constants.LUMENS_ASSET_CODE) {
            viewHolder.assetButton.text = context.getString(R.string.set_inflation_message)
            viewHolder.assetButton.setBackgroundColor(ContextCompat.getColor(context, R.color.mantis))
            viewHolder.assetButton.setOnClickListener {
                if (WalletApplication.localStore.balances!!.isNotEmpty() &&
                        AccountUtils.getTotalBalance(Constants.LUMENS_ASSET_TYPE).toDouble() > 1.0) {
                    context.startActivity(Intent(context, InflationActivity::class.java))
                } else {
                    showBalanceErrorDialog()
                }
            }
        } else if (asset.amount!!.toDouble() == 0.0) {
            viewHolder.assetButton.text = context.getString(R.string.remove_asset_message)
            viewHolder.assetButton.setBackgroundColor(ContextCompat.getColor(context, R.color.apricot))
            viewHolder.assetButton.setOnClickListener {
                listener.changeTrustline(asset.asset!!, true)
            }
        } else {
            viewHolder.assetButton.visibility = View.GONE
        }

        viewHolder.itemView.setOnClickListener {
            if (asset.code == Constants.LUMENS_ASSET_CODE) {
                WalletApplication.userSession.currAssetCode = Constants.LUMENS_ASSET_TYPE
                WalletApplication.userSession.currAssetName = Constants.LUMENS_ASSET_NAME
                WalletApplication.userSession.currAssetIssuer = ""
            } else {
                WalletApplication.userSession.currAssetCode = asset.code.toUpperCase()
                WalletApplication.userSession.currAssetName = asset.name
                WalletApplication.userSession.currAssetIssuer = asset.issuer
            }
            (context as Activity).finish()
        }
    }

    private fun configureAssetHeaderViewHolder(viewHolder : AssetHeaderViewHolder, position : Int) {
        val titleText = items[position] as String
        viewHolder.title.text = titleText
    }

    private fun configureSupportedAssetViewHolder(viewHolder: SupportedAssetViewHolder, position: Int) {
        val asset = items[position] as SupportedAsset
        val trustLineAsset = Asset.createNonNativeAsset(asset.code.toUpperCase(), KeyPair.fromAccountId(asset.issuer))

        viewHolder.assetName.text = String.format(context.getString(R.string.asset_template),
                asset.name, asset.code.toUpperCase())

        viewHolder.assetAmount.visibility = View.GONE
        Picasso.get().load(asset.image).into(viewHolder.assetImage)

        viewHolder.assetButton.text = context.getString(R.string.add_asset)
        viewHolder.assetButton.setBackgroundColor(ContextCompat.getColor(context, R.color.mantis))
        viewHolder.assetButton.setOnClickListener {
            listener.changeTrustline(trustLineAsset, false)
        }
        viewHolder.assetButton.visibility = View.VISIBLE
    }

    //endregion

    //region User Interface
    private fun showBalanceErrorDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.no_balance_dialog_title))
                .setMessage(context.getString(R.string.no_balance_text_message))
                .setPositiveButton(context.getString(R.string.ok)) { _, _ -> }
        val dialog = builder.create()
        dialog.show()
    }
    //endregion

}
