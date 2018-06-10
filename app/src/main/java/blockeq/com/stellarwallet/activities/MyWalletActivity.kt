package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import blockeq.com.stellarwallet.R
import kotlinx.android.synthetic.main.activity_wallets.*

class MyWalletActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_wallet)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}
