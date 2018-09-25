package blockeq.com.stellarwallet.activities

import android.os.Bundle
import blockeq.com.stellarwallet.R
import kotlinx.android.synthetic.main.content_wallets.*

class WalletsActivity : BasePopupActivity() {

    override fun setContent(): Int {
        return R.layout.content_wallets
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
    }

    override fun setupUI() {
        titleText.text = getString(R.string.asset_title_text)
    }
    //endregion

}
