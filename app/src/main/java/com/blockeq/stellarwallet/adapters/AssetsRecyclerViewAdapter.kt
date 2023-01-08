package com.blockeq.stellarwallet.adapters

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
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.activities.InflationActivity
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.interfaces.ChangeTrustlineListener
import com.blockeq.stellarwallet.models.DefaultAsset
import com.blockeq.stellarwallet.models.SessionAssetImpl
import com.blockeq.stellarwallet.models.SupportedAsset
import com.blockeq.stellarwallet.models.SupportedAssetType
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.StringFormat
import com.squareup.picasso.Picasso
import org.stellar.sdk.Asset
import org.stellar.sdk.KeyPair
import java.util.*
import kotlin.collections.ArrayList

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
        val defaultImage : TextView = v.findViewById(R.id.defaultAssetView)
        val assetName : TextView = v.findViewById(R.id.assetNameTextView)
        val assetAmount : TextView = v.findViewById(R.id.assetAmountTextView)
        val assetButton : Button = v.findViewById(R.id.assetButton)
    }

    class AssetHeaderViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        val title : TextView = v.findViewById(R.id.titleText)
    }

    class SupportedAssetViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        val assetImage : ImageView = v.findViewById(R.id.assetImageView)
        val defaultImage : TextView = v.findViewById(R.id.defaultAssetView)
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
                StringFormat.truncateDecimalPlaces(asset.amount), asset.code.toUpperCase(Locale.getDefault()))

        if (asset.image.isNotEmpty()) {
            viewHolder.defaultImage.visibility = View.GONE
            viewHolder.assetImage.visibility = View.VISIBLE
            Picasso.get().load(asset.image).into(viewHolder.assetImage)
        } else {
            viewHolder.defaultImage.text = asset.name[0].toString()
            viewHolder.defaultImage.visibility = View.VISIBLE
            viewHolder.assetImage.visibility = View.GONE
        }

        if (asset.code == Constants.LUMENS_ASSET_CODE) {
            //TODO: disabling inflation since in protocol 12 will be removed.
            viewHolder.assetButton.visibility = View.GONE

            viewHolder.assetButton.text = context.getString(R.string.set_inflation_message)
            viewHolder.assetButton.setBackgroundColor(ContextCompat.getColor(context, R.color.mantis))
            viewHolder.assetButton.setOnClickListener {
                if (WalletApplication.wallet.getBalances().isNotEmpty() &&
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
                WalletApplication.userSession.setSessionAsset(DefaultAsset())
            } else {
                WalletApplication.userSession.setSessionAsset(SessionAssetImpl(asset.code.toUpperCase(Locale.getDefault()), asset.name, asset.issuer))
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
        val trustLineAsset = Asset.createNonNativeAsset(asset.code.toUpperCase(Locale.getDefault()), KeyPair.fromAccountId(asset.issuer).accountId)

        viewHolder.assetName.text = String.format(context.getString(R.string.asset_template),
                asset.name, asset.code.toUpperCase(Locale.getDefault()))

        viewHolder.assetAmount.visibility = View.GONE
        viewHolder.defaultImage.visibility = View.GONE

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
