package com.blockeq.stellarwallet.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.activities.AssetsActivity
import com.blockeq.stellarwallet.activities.BalanceSummaryActivity
import com.blockeq.stellarwallet.activities.ReceiveActivity
import com.blockeq.stellarwallet.activities.StellarAddressActivity
import com.blockeq.stellarwallet.adapters.WalletRecyclerViewAdapter
import com.blockeq.stellarwallet.models.*
import com.blockeq.stellarwallet.mvvm.effects.WalletViewModelPolling
import com.blockeq.stellarwallet.mvvm.effects.WalletViewState
import com.blockeq.stellarwallet.utils.DebugPreferencesHelper
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.fragment_wallet.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import org.stellar.sdk.responses.effects.EffectResponse
import timber.log.Timber


class WalletFragment : BaseFragment() {
    private lateinit var appContext : Context
    private lateinit var viewModel : WalletViewModelPolling
    private var state = WalletState.UNKNOWN
    private var lastEffectListSize = 0
    private var activeAsset : String = DefaultAsset().LUMENS_ASSET_NAME
    private var qrRendered = false

    companion object {
        private const val REFRESH_EFFECT_DELAY = 400L

        fun newInstance(): WalletFragment = WalletFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_wallet, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            appContext = it.applicationContext
            viewModel = ViewModelProviders.of(it).get(WalletViewModelPolling::class.java)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        walletRecyclerView.layoutManager = LinearLayoutManager(appContext)
        walletRecyclerView.adapter = createAdapter()

        if (DebugPreferencesHelper(view.context.applicationContext).isTestNetServerEnabled) {
            mainTitle.text = "Wallet (TEST-NET SERVER)"
        }

        updateState(WalletState.UPDATING)
        lastEffectListSize = 0

        // since closing the stream causes so many crashes let's disable the pull to refresh
        swipeRefresh_wallet.isEnabled = true
        initViewModels()

        swipeRefresh_wallet.setOnRefreshListener {
            updateState(WalletState.UPDATING)
            swipeRefresh_wallet.postDelayed({
                activity?.let {
                    if (!it.isFinishing) {
                        viewModel.forceRefresh()
                    }
                }
            }, REFRESH_EFFECT_DELAY)
        }

        receiveButton.setOnClickListener {
            receiveButton.isEnabled = false
            activity?.let { activityContext ->
                startActivity(Intent(activityContext, ReceiveActivity::class.java))
                activityContext.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        }

        sendButton.setOnClickListener {
            sendButton.isEnabled = false
            activity?.let { activityContext ->
                startActivity(StellarAddressActivity.toSend(it.context))
                activityContext.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        }

        fetching_wallet_image.setColorFilter(getColor(appContext, R.color.paleSky), PorterDuff.Mode.SRC_ATOP)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        qrRendered = false
    }

    private fun generateQRCode(data: String, imageView: ImageView, size: Int) {
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)

        imageView.setImageBitmap(tintImage(bitmap, getColor(appContext, R.color.paleSky)))
    }

    private fun tintImage(bitmap: Bitmap, color: Int): Bitmap {
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SCREEN)
        val bitmapResult = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapResult)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return bitmapResult
    }

    private fun createListWrapper(): WalletHeterogeneousWrapper {
        val time = System.currentTimeMillis()
        val list = WalletHeterogeneousWrapper()
        list.array.add(TotalBalance(WalletState.UPDATING,"Refreshing Wallet", "","Updating..."))
        list.array.add(AvailableBalance("XLM", null, "0"))
        list.array.add(Pair("Activity", "Amount"))
        list.hideAvailableBalance()
        val delta = System.currentTimeMillis() - time
        Timber.d("createListWrapper(), it took: $delta ms")
        return list
    }

    override fun onResume() {
        super.onResume()
        if (state == WalletState.ACTIVE) {
            receiveButton.isEnabled = true
            sendButton.isEnabled = true
        }
        viewModel.moveToForeGround()

    }

    override fun onStop() {
        super.onStop()
        viewModel.moveToBackground()
    }

    //region User Interface

    private fun initViewModels() {
        Timber.d("initViewModels called")
        viewModel.walletViewState(false).observe(this, Observer {
            it?.let { that ->
                Timber.d("observed = ${it.status}")
                when(that.status) {
                    WalletViewState.AccountStatus.ACTIVE  -> updateState(WalletState.ACTIVE, it)
                    WalletViewState.AccountStatus.ERROR  -> updateState(WalletState.ERROR, it)
                    WalletViewState.AccountStatus.UNFUNDED  -> updateState(WalletState.NOT_FUNDED, it)
                    else -> {
                        //nothing
                    }
                }
            }
        })
    }

    private fun updateState(newState : WalletState, viewState : WalletViewState? = null) {
        Timber.d("updating new state={$newState}")
        var listWrapper : WalletHeterogeneousWrapper = createListWrapper()
        doAsync {
            when(newState) {
                WalletState.ACTIVE -> {
                    noTransactionsTextView.visibility = View.GONE
                    viewState?.effectList?.let {
                        val numberEffects = it.size
                        Timber.d("ACTIVE effects = $numberEffects vs last event $lastEffectListSize")
                        lastEffectListSize = numberEffects
                        listWrapper = createListWithData(it, viewState.activeAssetCode, viewState.availableBalance!!, viewState.totalBalance!!)
                    }
                }
                WalletState.UPDATING -> {
                    //we had to hide the list of effects :(
                    listWrapper.updateTotalBalance(TotalBalance(newState,"Refreshing Wallet", "","Updating..."))
                    listWrapper.hidePair()
                    listWrapper.hideAvailableBalance()
                }
                WalletState.ERROR -> {
                    listWrapper.updateTotalBalance(TotalBalance(newState, "Error fetching from Horizon", "", "Error"))
                    listWrapper.hidePair()
                }
                WalletState.NOT_FUNDED -> {
                    listWrapper.updateTotalBalance(TotalBalance(newState, "Account Funding Required", "", "0.00"))
                    listWrapper.hideAvailableBalance()
                    listWrapper.hidePair()
                } else -> {
                //nothing
                }
            }
            runOnUiThread {
                activity?.let {
                    if (!it.isFinishing && walletRecyclerView != null) {
                        if (!qrRendered && viewState != null && qrCode != null) {
                            generateQRCode(viewState.accountId, qrCode, 500)
                            qrRendered = true
                        }

                        if (!listWrapper.array.isEmpty()) {
                            (walletRecyclerView.adapter as WalletRecyclerViewAdapter).setItems(listWrapper.array)
                            walletRecyclerView.adapter?.notifyDataSetChanged()
                        }
                        updatePlaceHolders(newState)
                    }
                }
            }
            state = newState
            viewState?.let {
                activeAsset = it.activeAssetCode
            }
        }
    }

    /**
     * run this in the ui thread
     */
    private fun updatePlaceHolders(newState : WalletState) {
        activity?.let {
            if (!it.isFinishing){
                when(newState) {
                    WalletState.NOT_FUNDED -> {
                        sendButton.isEnabled = false
                        receiveButton.isEnabled = true
                        noTransactionsTextView.visibility = View.GONE
                        fetchingState.visibility = View.GONE
                        fundingState.visibility = View.VISIBLE
                    }
                    WalletState.ERROR -> {
                        noTransactionsTextView.visibility = View.GONE
                        sendButton.isEnabled = false
                        receiveButton.isEnabled = false
                        fetchingState.visibility = View.VISIBLE
                        fundingState.visibility = View.GONE
                    }
                    WalletState.UPDATING -> {
                        noTransactionsTextView.visibility = View.GONE
                        sendButton.isEnabled = false
                        receiveButton.isEnabled = true
                        swipeRefresh_wallet.isRefreshing = false
                        fetchingState.visibility = View.VISIBLE
                        fundingState.visibility = View.GONE
                    } WalletState.ACTIVE -> {
                        sendButton.isEnabled = true
                        receiveButton.isEnabled = true
                        noTransactionsTextView.visibility = View.GONE
                        fetchingState.visibility = View.GONE
                        fundingState.visibility = View.GONE
                } else -> {
                        // nothing
                    }
                }
            }
        }
    }

    /**
     * It will reset the array list.
     */
    private fun createAdapter(): WalletRecyclerViewAdapter {
        val adapter = WalletRecyclerViewAdapter(activity!!)
        adapter.setOnAssetDropdownListener(object : WalletRecyclerViewAdapter.OnAssetDropdownListener {
            override fun onAssetDropdownClicked(view: View, position: Int) {
                val context = view.context
                startActivity(Intent(context, AssetsActivity::class.java))
                (context as Activity).overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        })
        adapter.setOnLearnMoreButtonListener(object : WalletRecyclerViewAdapter.OnLearnMoreButtonListener {
            override fun onLearnMoreButtonClicked(view: View, assetCode: String, issuer: String?, position: Int) {
                val context = view.context
                if (assetCode == "native" || issuer == null || issuer.isBlank()) {
                    startActivity(BalanceSummaryActivity.newNativeAssetIntent(context))
                } else {
                    startActivity(BalanceSummaryActivity.newIntent(context, assetCode, issuer))
                }
                (context as Activity).overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        })
        return adapter
    }

    private fun createListWithData(effects : ArrayList<EffectResponse>, activeAsset : String, availableBalance: AvailableBalance, totalAssetBalance : TotalBalance) : WalletHeterogeneousWrapper {
        val time = System.currentTimeMillis()
        val list = createListWrapper()
        list.showAvailableBalance(availableBalance)
        list.updateTotalBalance(totalAssetBalance)
        list.updateEffectsList(activeAsset, effects)
        list.updateAvailableBalance(availableBalance)
        val delta = System.currentTimeMillis() - time
        Timber.d("createListWithData(list{${effects.size}}, $activeAsset), it took: $delta ms")
        return list
    }

    //endregion

}
