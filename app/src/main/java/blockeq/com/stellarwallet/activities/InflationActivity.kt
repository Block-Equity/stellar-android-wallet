package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.interfaces.SuccessErrorCallback
import blockeq.com.stellarwallet.services.networking.Horizon

class InflationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inflation)

        setupUI()
    }

    override fun setupUI() {

        // In on button click listener
        Horizon.Companion.JoinInflationDestination(object : SuccessErrorCallback {
            override fun onSuccess() {
                Toast.makeText(this@InflationActivity, "Inflation destination set!", Toast.LENGTH_SHORT).show()
            }

            override fun onError() {
                Toast.makeText(this@InflationActivity, "There was an error setting inflation destination.", Toast.LENGTH_SHORT).show()
            }

        }).execute(WalletApplication.session!!.keyPair)
    }
}
