package com.blockeq.stellarwallet.fragments.tabs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.view.*
import android.widget.*
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.interfaces.AfterTextChanged
import com.blockeq.stellarwallet.interfaces.OnItemSelected
import com.blockeq.stellarwallet.interfaces.OnTradeCurrenciesChanged
import com.blockeq.stellarwallet.interfaces.OnUpdateTradeTab
import com.blockeq.stellarwallet.models.AssetUtil
import com.blockeq.stellarwallet.models.Currency
import com.blockeq.stellarwallet.models.SelectionModel
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.DebugPreferencesHelper
import kotlinx.android.synthetic.main.fragment_tab_trade.*
import kotlinx.android.synthetic.main.view_custom_selector.view.*
import org.stellar.sdk.Asset
import org.stellar.sdk.responses.OrderBookResponse
import timber.log.Timber
import java.text.DecimalFormat
import java.text.NumberFormat


class TradeTabFragment : Fragment(), View.OnClickListener, OnUpdateTradeTab {

    private lateinit var appContext : Context

    private var sellingCurrencies = mutableListOf<SelectionModel>()
    private var buyingCurrencies = mutableListOf<SelectionModel>()
    private var holdingsAmount : Double = 0.0
    private lateinit var listener: OnTradeCurrenciesChanged
    private lateinit var selectedSellingCurrency: SelectionModel
    private lateinit var selectedBuyingCurrency: SelectionModel
    private var addedCurrencies : ArrayList<Currency> = ArrayList()
    private var latestBid: OrderBookResponse.Row? = null
    private var orderType : OrderType = OrderType.MARKET
    private var dataAvailable = false
    private lateinit var toolTip : PopupWindow
    private var isToolTipShowing = false
    private val ZERO_VALUE = "0.0"
    private val decimalFormat : NumberFormat = DecimalFormat("0.#######")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_trade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appContext = view.context.applicationContext
        toolTip = PopupWindow(view.context)
        setBuyingSelectorEnabled(false)
        refreshAddedCurrencies()
        setupListeners()

        Timber.d("TradeTabFragment{$this}#onViewCreated")
    }

    private fun setupListeners() {
        toggleMarket.setOnClickListener(this)
        toggleLimit.setOnClickListener(this)
        tenth.setOnClickListener(this)
        quarter.setOnClickListener(this)
        half.setOnClickListener(this)
        threeQuarters.setOnClickListener(this)
        all.setOnClickListener(this)
        submitTrade.setOnClickListener(this)

        sellingCustomSelector.editText.addTextChangedListener(object : AfterTextChanged() {
            override fun afterTextChanged(editable: Editable) {
                updateBuyingValueIfNeeded()
                refreshSubmitTradeButton()
            }
        })

        sellingCustomSelector.setSelectionValues(sellingCurrencies)
        sellingCustomSelector.spinner.onItemSelectedListener = object : OnItemSelected() {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSellingCurrency = sellingCurrencies[position]
                holdingsAmount = selectedSellingCurrency.holdings

                if (selectedSellingCurrency.label == AssetUtil.NATIVE_ASSET_CODE) {
                    val available = WalletApplication.wallet.getAvailableBalance().toDouble()

                    holdings.text = String.format(getString(R.string.holdings_amount),
                            decimalFormat.format(available),
                            selectedSellingCurrency.label)

                    holdingsAmount = available

                } else {
                    holdings.text = String.format(getString(R.string.holdings_amount),
                            decimalFormat.format(holdingsAmount),
                            selectedSellingCurrency.label)
                }

                resetBuyingCurrencies()
                buyingCurrencies.removeAt(position)

                buyingCustomSelector.setSelectionValues(buyingCurrencies)

                onSelectorChanged()
            }
        }

        buyingCustomSelector.setSelectionValues(buyingCurrencies)
        buyingCustomSelector.spinner.onItemSelectedListener = object : OnItemSelected(){
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBuyingCurrency = buyingCurrencies[position]
                onSelectorChanged()
            }
        }
    }

    private fun onSelectorChanged() {
        dataAvailable = false
        if (::selectedBuyingCurrency.isInitialized && ::selectedSellingCurrency.isInitialized) {
            notifyParent(selectedSellingCurrency, selectedBuyingCurrency)
        }
        refreshSubmitTradeButton()
        updateBuyingValueIfNeeded()
    }

    private fun refreshSubmitTradeButton() {
        val sellingValue = sellingCustomSelector.editText.text.toString()
        val buyingValue = sellingCustomSelector.editText.text.toString()

        var numberFormatValid = true
        var sellingValueDouble = 0.toDouble()
        try {
            sellingValueDouble = sellingValue.toDouble()
            buyingValue.toDouble()
        } catch (e : NumberFormatException) {
            Timber.d("selling or buying value have not a valid format")
            numberFormatValid = false
        }

        if (sellingValue.isEmpty() || buyingValue.isEmpty() ||
                !numberFormatValid || sellingValueDouble.compareTo(0) == 0 ) {
           submitTrade.isEnabled = false
        } else if(::selectedSellingCurrency.isInitialized){
            submitTrade.isEnabled = sellingValue.toDouble() <= selectedSellingCurrency.holdings
        }
    }

    private fun updateBuyingValueIfNeeded() {
        if (sellingCustomSelector == null) return
        val stringText = sellingCustomSelector.editText.text.toString()
        if (stringText.isEmpty()) {
            buyingCustomSelector.editText.text.clear()
            return
        }

        if (orderType == OrderType.MARKET) {
            if (latestBid != null && dataAvailable) {
                val value = sellingCustomSelector.editText.text.toString().toFloatOrNull()
                val priceR = latestBid?.priceR
                if (value != null && priceR != null) {
                    val floatValue : Float = value.toFloat()
                    val stringValue = decimalFormat.format(priceR.numerator*floatValue/priceR.denominator*0.9999)
                    buyingCustomSelector.editText.setText(stringValue)
                }
            } else {
                buyingCustomSelector.editText.setText(ZERO_VALUE)
            }
        }
    }

    private fun notifyParent(selling : SelectionModel?, buying : SelectionModel?) {
        if (selling != null && buying != null) {
            listener.onCurrencyChange(selling, buying)
        }
    }

    enum class OrderType {
        LIMIT,
        MARKET
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (toolTip.isShowing) {
            toolTip.dismiss()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tenth -> sellingCustomSelector.editText.setText(decimalFormat.format(0.1 * holdingsAmount).toString())
            R.id.quarter -> sellingCustomSelector.editText.setText(decimalFormat.format(0.25 * holdingsAmount).toString())
            R.id.half -> sellingCustomSelector.editText.setText(decimalFormat.format(0.5 * holdingsAmount).toString())
            R.id.threeQuarters -> sellingCustomSelector.editText.setText(decimalFormat.format(0.75 * holdingsAmount).toString())
            R.id.all -> sellingCustomSelector.editText.setText(decimalFormat.format(holdingsAmount))
            R.id.toggleMarket -> {
                orderType = OrderType.MARKET
                toggleMarket.setTextColor(ContextCompat.getColor(view.context, R.color.white))
                toggleMarket.setBackgroundResource(R.drawable.left_toggle_selected)
                toggleLimit.setBackgroundResource(R.drawable.right_toggle)
                toggleLimit.setTextColor(ContextCompat.getColor(view.context, R.color.black))

                setBuyingSelectorEnabled(false)
                updateBuyingValueIfNeeded()
            }
            R.id.toggleLimit -> {
                orderType = OrderType.LIMIT
                toggleLimit.setBackgroundResource(R.drawable.right_toggle_selected)
                toggleLimit.setTextColor(ContextCompat.getColor(view.context, R.color.white))
                toggleMarket.setBackgroundResource(R.drawable.left_toggle)
                toggleMarket.setTextColor(ContextCompat.getColor(view.context, R.color.black))
                setBuyingSelectorEnabled(true)
            }
            R.id.submitTrade -> {
                if (buyingCustomSelector.editText.text.toString().isEmpty()) {
                    createSnackBar("Buying price can not be empty.", Snackbar.LENGTH_SHORT).show()
                } else if ((orderType == OrderType.MARKET && !dataAvailable) || buyingCustomSelector.editText.text.toString() == ZERO_VALUE) {
                    // buyingEditText should be empty at this moment

                    createSnackBar("Trade price cannot be 0. Please override limit order.", Snackbar.LENGTH_SHORT).show()

                } else {

                    val dialogBuilder = AlertDialog.Builder(view.context)
                    dialogBuilder.setTitle("Confirm Trade")

                    val sellingText = sellingCustomSelector.editText.text.toString()
                    val sellingCode = selectedSellingCurrency.label
                    val buyingText = buyingCustomSelector.editText.text.toString()
                    val buyingCode = selectedBuyingCurrency.label

                    dialogBuilder.setMessage("You are about to submit a trade of $sellingText $sellingCode for $buyingText $buyingCode.")
                    dialogBuilder.setPositiveButton("Submit") { _, _ ->
                        proceedWithTrade(buyingText, sellingText, selectedSellingCurrency.asset!!, selectedBuyingCurrency.asset!!)
                    }

                    dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }

                    dialogBuilder.show()
                }
            }
        }
    }

    private fun createSnackBar(text : CharSequence, duration: Int) : Snackbar {
        return Snackbar.make(activity!!.findViewById(R.id.content_container), text, duration)
    }

    private fun proceedWithTrade(buyingAmount :String, sellingAmount :String, sellingAsset : Asset, buyingAsset: Asset) {
        val snackbar =  createSnackBar("Submitting order",  Snackbar.LENGTH_INDEFINITE)
        val snackView = snackbar.view as Snackbar.SnackbarLayout
        val progress = ProgressBar(context)
        val height = resources.getDimensionPixelOffset(R.dimen.progress_snackbar_height)
        val width = resources.getDimensionPixelOffset(R.dimen.progress_snackbar_width)

        val params = FrameLayout.LayoutParams(height, width)
        params.gravity = Gravity.END or Gravity.RIGHT or Gravity.CENTER_VERTICAL
        val margin = resources.getDimensionPixelOffset(R.dimen.progress_snackbar_margin)
        progress.setPadding(margin, margin, margin, margin)
        snackView.addView(progress, params)
        snackbar.show()

        submitTrade.isEnabled = false

        setBuyingSelectorEnabled(false)
        setSellingSelectorEnabled(false)

        WalletApplication.userSession.getAvailableBalance()

        val sellingAmountFormatted = decimalFormat.format(sellingAmount.toDouble())
        val priceFormatted = decimalFormat.format(buyingAmount.toDouble() / sellingAmount.toDouble())

        Horizon.getCreateMarketOffer(object: Horizon.OnMarketOfferListener {
            override fun onExecuted() {
                snackbar.dismiss()
                createSnackBar("Order executed", Snackbar.LENGTH_SHORT).show()

                submitTrade.isEnabled = true
                setSellingSelectorEnabled(true)
                setBuyingSelectorEnabled(true)
            }

            override fun onFailed(errorMessage : String) {
                snackbar.dismiss()

                createSnackBar("Order failed: $errorMessage", Snackbar.LENGTH_SHORT).show()

                submitTrade.isEnabled = true
                setSellingSelectorEnabled(false)
                setBuyingSelectorEnabled(false)
            }
        }, AccountUtils.getSecretSeed(appContext), sellingAsset, buyingAsset,
                sellingAmountFormatted, priceFormatted)
    }


    private fun setSellingSelectorEnabled(isEnabled: Boolean) {
        sellingCustomSelector.editText.isEnabled = isEnabled
    }

    private fun setBuyingSelectorEnabled(isEnabled: Boolean) {
        buyingCustomSelector.editText.isEnabled = isEnabled
        if(isEnabled) {
            buyingCustomSelector.editTextMask.visibility = View.GONE
            buyingCustomSelector.editTextMask.setOnClickListener(null)
        } else {
            buyingCustomSelector.editTextMask.visibility = View.VISIBLE

            buyingCustomSelector.editTextMask.setOnClickListener {
                if (DebugPreferencesHelper(appContext).isTradeTooltipEnabled) {
                    displayPopupWindow(toggleLimit)
                }
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            listener = parentFragment as OnTradeCurrenciesChanged
        } catch (e: ClassCastException) {
            Timber.e("the parent activity must implement: %s", OnTradeCurrenciesChanged::class.java.simpleName)
        }
    }

    private fun resetBuyingCurrencies() {
        buyingCurrencies.clear()
        addedCurrencies.forEach {
            buyingCurrencies.add(it)
        }
    }

    private fun refreshAddedCurrencies() {
        val accounts = WalletApplication.wallet.getBalances()
        addedCurrencies.clear()
        var i = 0
        var native : Currency? = null
        accounts.forEach { it ->
            val currency = if(it.assetType != "native") {
                Currency(i, it.assetCode, it.assetCode, it.balance.toDouble(), it.asset)
            } else {
                native = Currency(i, AssetUtil.NATIVE_ASSET_CODE, "LUMEN", it.balance.toDouble(), it.asset)
                native as Currency
            }
            addedCurrencies.add(currency)
            i++
        }

        native?.let {
            addedCurrencies.remove(it)
            addedCurrencies.add(0, it)
        }

        sellingCurrencies.clear()
        buyingCurrencies.clear()
        addedCurrencies.forEach {
            sellingCurrencies.add(it)
            buyingCurrencies.add(it)
        }
    }

    override fun onLastOrderBookUpdated(asks: Array<OrderBookResponse.Row>, bids: Array<OrderBookResponse.Row>) {
        if (bids.isNotEmpty()) {
           latestBid = bids[0]
           dataAvailable = true
           updateBuyingValueIfNeeded()
        } else {
           dataAvailable = false
        }
    }

    private fun displayPopupWindow(anchorView: View) {
        if (!toolTip.isShowing) {
            @SuppressLint("InflateParams")
            val layout = layoutInflater.inflate(R.layout.popup_content, null)
            toolTip.contentView = layout
            // Set content width and height
            toolTip.height = WindowManager.LayoutParams.WRAP_CONTENT
            toolTip.width = WindowManager.LayoutParams.WRAP_CONTENT
            // Show anchored to button
            toolTip.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            toolTip.showAsDropDown(anchorView)
            toolTip.setOnDismissListener {
                isToolTipShowing = false
            }
            isToolTipShowing = true
            anchorView.postDelayed({
                if (toolTip.isShowing) {
                    toolTip.dismiss()
                }
            }, 800)
        }
    }

}