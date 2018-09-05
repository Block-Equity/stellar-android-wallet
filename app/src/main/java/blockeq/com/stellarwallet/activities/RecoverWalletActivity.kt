package blockeq.com.stellarwallet.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import blockeq.com.stellarwallet.R
import kotlinx.android.synthetic.main.activity_recover_wallet.*


class RecoverWalletActivity : AppCompatActivity() {

    val PIN_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_wallet)

        setSupportActionBar(findViewById(R.id.recoverToolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        nextButton.setOnClickListener {
            startActivityForResult(Intent(this, PinActivity::class.java), PIN_REQUEST_CODE)
            overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIN_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
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
