package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import blockeq.com.stellarwallet.R
import kotlinx.android.synthetic.main.activity_wallets.*

class WalletsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallets)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        cancelButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
        }
    }

}
