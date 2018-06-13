package blockeq.com.stellarwallet.activities

import blockeq.com.stellarwallet.R

class MyWalletActivity : BasePopupActivity() {

    override fun setTitle(): Int {
        return R.string.title_activity_my_wallet
    }

    override fun setContent(): Int {
        return R.layout.content_my_wallet
    }

}
