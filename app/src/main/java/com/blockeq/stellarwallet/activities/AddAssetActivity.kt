package com.blockeq.stellarwallet.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.interfaces.SuccessErrorCallback
import com.blockeq.stellarwallet.models.HorizonException
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.NetworkUtils
import kotlinx.android.synthetic.main.activity_add_asset.*
import org.stellar.sdk.Asset
import org.stellar.sdk.KeyPair

class AddAssetActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_asset)

        setupUI()
    }

    fun setupUI() {
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        addAssetButton.setOnClickListener {
            if (assetCodeEditText.text.isNotEmpty() && addressEditText.text.isNotEmpty()) {
                val secretSeed = AccountUtils.getSecretSeed(this)
                val asset : Asset
                try {
                    asset = Asset.createNonNativeAsset(assetCodeEditText.text.toString().toUpperCase(),
                            KeyPair.fromAccountId(addressEditText.text.toString().toUpperCase()).accountId)

                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "Invalid input for code or issuer", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                progressBar.visibility = View.VISIBLE
                changeTrustLine(secretSeed, asset)
            } else {
                Toast.makeText(applicationContext, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun changeTrustLine(secretSeed: CharArray, assetToChange: Asset) {
        if (NetworkUtils(this).isNetworkAvailable()) {
            Horizon.getChangeTrust(object : SuccessErrorCallback {
                override fun onSuccess() {
                    Toast.makeText(this@AddAssetActivity, getString(R.string.asset_added), Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    finish()
                }

                override fun onError(error: HorizonException) {
                    Toast.makeText(this@AddAssetActivity, error.message(this@AddAssetActivity), Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }, assetToChange, false, secretSeed).execute()
        } else {
            NetworkUtils(this).displayNoNetwork()
            progressBar.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            if (item.itemId == android.R.id.home) {
                finish()
                return true
            }
        }
        return false
    }
}
