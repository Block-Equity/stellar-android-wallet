package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.interfaces.SuccessErrorCallback
import blockeq.com.stellarwallet.services.networking.Horizon
import blockeq.com.stellarwallet.utils.AccountUtils
import blockeq.com.stellarwallet.utils.NetworkUtils
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

                    override fun onError() {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@InflationActivity, getString(R.string.inflation_set_error), Toast.LENGTH_SHORT).show()
                    }
                }, secretSeed, addressEditText.text.toString()).execute()
            } else {
                progressBar.visibility = View.GONE
                NetworkUtils(this).displayNoNetwork()
            }
        }
    }
}
