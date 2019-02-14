package com.blockeq.stellarwallet.fragments.tabs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.arch.lifecycle.Observer
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
import com.blockeq.stellarwallet.interfaces.*
import com.blockeq.stellarwallet.models.AssetUtil
import com.blockeq.stellarwallet.models.Currency
import com.blockeq.stellarwallet.models.SelectionModel
import com.blockeq.stellarwallet.mvvm.account.AccountRepository
import com.blockeq.stellarwallet.mvvm.balance.BalanceRepository
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.DebugPreferencesHelper
import kotlinx.android.synthetic.main.fragment_tab_trade.*
import kotlinx.android.synthetic.main.view_custom_selector.view.*
import org.stellar.sdk.Asset
import org.stellar.sdk.responses.OrderBookResponse
import timber.log.Timber
import java.text.DecimalFormat

class TradeTabFragment : Fragment(), View.OnClickListener, OnUpdateTradeTab {
    private lateinit var appContext : Context
    private lateinit var parentListener: OnTradeCurrenciesChanged
    private lateinit var selectedSellingCurrency: SelectionModel
    private lateinit var selectedBuyingCurrency: SelectionModel
    private lateinit var toolTip : PopupWindow

    private var sellingCurrencies = mutableListOf<SelectionModel>()
    private var buyingCurrencies = mutableListOf<SelectionModel>()
    private var holdingsAmount : Double = 0.0
    private var addedCurrencies : ArrayList<Currency> = ArrayList()
    private var latestBid: OrderBookResponse.Row? = null
    private var orderType : OrderType = OrderType.MARKET
    private var dataAvailable = false
    private var isToolTipShowing = false
    private val ZERO_VALUE = "0.0"
    private val decimalFormat = DecimalFormat("0.#######")

    private var balance : BalanceAvailability? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_trade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("view created")
        appContext = view.context.applicationContext
        toolTip = PopupWindow(view.context)
        setBuyingSelectorEnabled(false)
        setupListeners()

        BalanceRepository.loadBalance().observe(this, Observer {
            if(it != null) {
                balance = it
                refreshAddedCurrencies()
                setupSpinners()
                Timber.d("new balance")
                if (::selectedSellingCurrency.isInitialized) {
                    sellingCurrencies.forEach { selection ->
                        if (selection.label == selectedSellingCurrency.label) {
                            refreshBalance(selection.holdings)
                        }
                    }
                    buyingCustomSelector.editText.setText("")
                    sellingCustomSelector.editText.setText("")
                    refreshSubmitTradeButton()
                    updateBuyingValueIfNeeded()
                }
            }
        })
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
    }

    private fun setupSpinners() {
        sellingCustomSelector.setSelectionValues(sellingCurrencies)
        sellingCustomSelector.spinner.onItemSelectedListener = object : OnItemSelected() {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSellingCurrency = sellingCurrencies[position]
                refreshBalance(selectedSellingCurrency.holdings)

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

    private fun applyNativeFees(amount : Double): Double {
        // it will reserve double the network fee to be able to cancel the 100% open offer
        val value = amount -0.5 -0.0002
        if (value < 0) return 0.00
        return value
    }

    private fun refreshBalance(holding: Double) {
        var availableForTrading = holding
        if (selectedSellingCurrency.label == "XLM") {
            availableForTrading = applyNativeFees(holding)
        }

        var string = String.format(getString(R.string.holdings_amount),
                decimalFormat.format(availableForTrading),
                selectedSellingCurrency.label)

        if (selectedSellingCurrency.label == "XLM") {
            string += " available"
        }

        holdings.text = string

        holdingsAmount = availableForTrading
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
            parentListener.onCurrencyChange(selling, buying)
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
                    createSnackBar("Buying price can not be empty.", Snackbar.LENGTH_SHORT)?.show()
                } else if ((orderType == OrderType.MARKET && !dataAvailable) || buyingCustomSelector.editText.text.toString() == ZERO_VALUE) {
                    // buyingEditText should be empty at this moment

                    createSnackBar("Trade price cannot be 0. Please override limit order.", Snackbar.LENGTH_SHORT)?.show()

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

    private fun createSnackBar(text : CharSequence, duration: Int) : Snackbar? {
        activity?.let {
            return Snackbar.make(it.findViewById(R.id.content_container), text, duration)
        }
        return null
    }

    private fun proceedWithTrade(buyingAmount :String, sellingAmount :String, sellingAsset : Asset, buyingAsset: Asset) {
        val snackbar = createSnackBar("Submitting order",  Snackbar.LENGTH_INDEFINITE)
        val snackView = snackbar?.view as Snackbar.SnackbarLayout
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

        val sellingAmountFormatted = decimalFormat.format(sellingAmount.toDouble())
        val priceFormatted = decimalFormat.format(buyingAmount.toDouble() / sellingAmount.toDouble())

        Horizon.getCreateMarketOffer(object: Horizon.OnMarketOfferListener {
            override fun onExecuted() {
                snackbar.dismiss()
                createSnackBar("Order executed", Snackbar.LENGTH_SHORT)?.show()

                submitTrade.isEnabled = true
                setSellingSelectorEnabled(true)
                setBuyingSelectorEnabled(true)
                BalanceRepository.refresh()
                sellingCustomSelector.editText.setText("0.0")
                buyingCustomSelector.editText.setText("0.0")

            }

            override fun onFailed(errorMessage : String) {
                snackbar.dismiss()

                createSnackBar("Order failed: $errorMessage", Snackbar.LENGTH_SHORT)?.show()

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
        if (isEnabled) {
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
            parentListener = parentFragment as OnTradeCurrenciesChanged
        } catch (e: ClassCastException) {
            Timber.e("the parent must implement: %s", OnTradeCurrenciesChanged::class.java.simpleName)
        }
    }

    private fun resetBuyingCurrencies() {
        buyingCurrencies.clear()
        addedCurrencies.forEach {
            buyingCurrencies.add(it)
        }
    }

    private fun refreshAddedCurrencies() {
        if (balance == null) {
            return
        }
        addedCurrencies.clear()
        var i = 0
        var native: Currency? = null
        balance?.let {
            it.getAllBalances().forEach { that ->
                val currency = if (that.assetCode == "XLM") {
                    native = Currency(i, AssetUtil.NATIVE_ASSET_CODE, "LUMEN", that.totalAvailable.toDouble(), that.asset)
                    native as Currency
                } else {
                    Currency(i, that.assetCode, that.assetCode, that.totalAvailable.toDouble(), that.asset)
                }
                addedCurrencies.add(currency)
                i++
            }

            native?.let { currency ->
                addedCurrencies.remove(currency)
                addedCurrencies.add(0, currency)
            }

            sellingCurrencies.clear()
            buyingCurrencies.clear()
            addedCurrencies.forEach { added ->
                sellingCurrencies.add(added)
                buyingCurrencies.add(added)
            }
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