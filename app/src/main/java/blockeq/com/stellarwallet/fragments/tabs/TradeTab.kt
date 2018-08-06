package blockeq.com.stellarwallet.fragments.tabs

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.reusables.models.Currency
import blockeq.com.stellarwallet.reusables.models.SelectionModel
import kotlinx.android.synthetic.main.fragment_tab_trade.*


class TradeTab : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_trade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleMarket.setOnClickListener(this)
        toggleLimit.setOnClickListener(this)

        // Mockup Data
        val xlm = Currency(1, "XLM", "Stellar")
        val cad = Currency(2, "CAD", "Canadian Dollar")
        val usd = Currency(3, "USD", "US Dollar")
        val btc = Currency(4, "BTC", "Bitcoin")
        val sellingCurrencies: Array<SelectionModel> = arrayOf(xlm, cad, usd, btc)
        val buyingCurrencies = sellingCurrencies
        //

        sellingCustomSelector.setSelectionValues(sellingCurrencies)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.toggleMarket -> {
                toggleMarket.setBackgroundResource(R.drawable.left_toggle_selected)
                toggleLimit.setBackgroundResource(R.drawable.right_toggle)
            }
            R.id.toggleLimit -> {
                toggleLimit.setBackgroundResource(R.drawable.right_toggle_selected)
                toggleMarket.setBackgroundResource(R.drawable.left_toggle)
            }
        }
    }
}