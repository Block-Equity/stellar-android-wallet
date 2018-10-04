package blockeq.com.stellarwallet.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.interfaces.SuccessErrorCallback
import blockeq.com.stellarwallet.models.PinType
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
            launchPINView(PinType.CHECK, "", "", false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinActivity.PIN_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val secretSeed = data!!.getCharArrayExtra(PinActivity.KEY_SECRET_SEED)

                    if (NetworkUtils(this).isNetworkAvailable()) {
                        Horizon.Companion.JoinInflationDestination(object : SuccessErrorCallback {
                            override fun onSuccess() {
                                Toast.makeText(this@InflationActivity, getString(R.string.inflation_set_success), Toast.LENGTH_SHORT).show()
                            }

                            override fun onError() {
                                Toast.makeText(this@InflationActivity, getString(R.string.inflation_set_error), Toast.LENGTH_SHORT).show()
                            }
                        }, secretSeed, addressEditText.text.toString()).execute()
                    } else {
                        NetworkUtils(this).displayNoNetwork()
                    }
                }
                Activity.RESULT_CANCELED -> {}
                else -> finish()
            }
        }
    }
}
