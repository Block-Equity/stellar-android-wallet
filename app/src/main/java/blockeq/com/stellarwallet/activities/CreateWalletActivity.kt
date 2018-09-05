package blockeq.com.stellarwallet.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.fragments.SettingsFragment
import kotlinx.android.synthetic.main.activity_create_wallet.*

class CreateWalletActivity : AppCompatActivity() {

    val PIN_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wallet)

        setSupportActionBar(findViewById(R.id.createToolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        confirmButton.setOnClickListener {
            startActivityForResult(Intent(this, PinActivity::class.java), PIN_REQUEST_CODE)
            overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SettingsFragment.PIN_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                Activity.RESULT_CANCELED -> finish()
                else -> finish()
            }
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
