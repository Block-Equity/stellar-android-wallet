package com.blockeq.stellarwallet.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.interfaces.SuccessErrorCallback
import com.blockeq.stellarwallet.models.HorizonException
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.NetworkUtils
import kotlinx.android.synthetic.main.activity_inflation.*

class InflationActivity : BasePopupActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
    }

    override fun setContent(): Int {
        return R.layout.activity_inflation
    }

    private fun setupUI() {

        addressEditText.setText(Constants.INFLATION_DESTINATION)

        saveButton.setOnClickListener {

            progressBar.visibility = View.VISIBLE
            val secretSeed = AccountUtils.getSecretSeed(it.context.applicationContext)

            if (NetworkUtils(this).isNetworkAvailable()) {
                Horizon.getJoinInflationDestination(object : SuccessErrorCallback {
                    override fun onSuccess() {
                        Toast.makeText(this@InflationActivity, getString(R.string.inflation_set_success), Toast.LENGTH_SHORT).show()
                        finish()
                    }

                    override fun onError(error: HorizonException) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@InflationActivity, error.message(this@InflationActivity), Toast.LENGTH_SHORT).show()
                    }
                }, secretSeed, addressEditText.text.toString()).execute()
            } else {
                progressBar.visibility = View.GONE
                NetworkUtils(this).displayNoNetwork()
            }
        }
    }
}
