package blockeq.com.stellarwallet.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.activities.MyWalletActivity
import blockeq.com.stellarwallet.activities.WalletsActivity
import kotlinx.android.synthetic.main.fragment_wallet.*

class WalletFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_wallet, container, false)

    companion object {
        fun newInstance(): WalletFragment = WalletFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assetsButton.setOnClickListener {
            startActivity(Intent(activity, WalletsActivity::class.java))
            activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
        sendButton.setOnClickListener {
            startActivity(Intent(activity, MyWalletActivity::class.java))
            activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
    }
}