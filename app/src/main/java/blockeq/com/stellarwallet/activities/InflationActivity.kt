package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.interfaces.SuccessErrorCallback
import blockeq.com.stellarwallet.services.networking.Horizon
import blockeq.com.stellarwallet.utils.NetworkUtils
import kotlinx.android.synthetic.main.activity_inflation.*

class InflationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inflation)

        setupUI()
    }

    override fun setupUI() {

        addressEditText.setText(Constants.INFLATION_DESTINATION)

        saveButton.setOnClickListener {
            if (NetworkUtils(this).isNetworkAvailable()) {
                Horizon.Companion.JoinInflationDestination(object : SuccessErrorCallback {
                    override fun onSuccess() {
                        Toast.makeText(this@InflationActivity, "Inflation destination set!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError() {
                        Toast.makeText(this@InflationActivity, "There was an error setting inflation destination.", Toast.LENGTH_SHORT).show()
                    }

                }, addressEditText.text.toString()).execute()
            } else {
                NetworkUtils(this).displayNoNetwork()
            }
        }
    }
}
