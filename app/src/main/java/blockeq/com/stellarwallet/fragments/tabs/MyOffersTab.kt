package blockeq.com.stellarwallet.fragments.tabs

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import blockeq.com.stellarwallet.R


class MyOffersTab : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_my_offers, container, false)
    }
}