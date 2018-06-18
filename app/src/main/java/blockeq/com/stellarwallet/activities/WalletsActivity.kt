package blockeq.com.stellarwallet.activities

import blockeq.com.stellarwallet.R

class WalletsActivity : BasePopupActivity() {

    override fun setTitle(): Int {
        return R.string.title_activity_wallets
    }

    override fun setContent(): Int {
        return R.layout.content_wallets
    }

}
