package com.blockeq.stellarwallet.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
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
import com.blockeq.stellarwallet.models.AvailableBalance
import com.blockeq.stellarwallet.models.WalletState
import com.blockeq.stellarwallet.models.TotalBalance
import com.blockeq.stellarwallet.models.WalletHeterogeneousArray
import com.blockeq.stellarwallet.mvvm.effects.WalletViewModel
import com.blockeq.stellarwallet.mvvm.effects.WalletViewState
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.fragment_wallet.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.uiThread
import org.stellar.sdk.responses.effects.EffectResponse
import timber.log.Timber
import android.support.v4.content.ContextCompat.getColor
import android.graphics.*
import android.support.v7.widget.RecyclerView

class WalletFragment : BaseFragment() {

    private lateinit var recyclerViewArrayList: WalletHeterogeneousArray
    private lateinit var viewModel : WalletViewModel
    private var state = WalletState.UNKNOWN

    companion object {
        private const val REFRESH_EFFECT_DELAY = 400L

        fun newInstance(): WalletFragment = WalletFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_wallet, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WalletViewModel::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        walletRecyclerView.layoutManager = LinearLayoutManager(activity)

        resetList()
        updateState(WalletState.UPDATING)
        // since closing the stream causes so many crashes let's disable the pull to refresh
        swipeRefresh_wallet.isEnabled = false
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

    }

    private fun generateQRCode(data: String, imageView: ImageView, size: Int) {
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)

        imageView.setImageBitmap(tintImage(bitmap, getColor(context!!, R.color.paleSky)))
    }

    private fun tintImage(bitmap: Bitmap, color: Int): Bitmap {
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SCREEN)
        val bitmapResult = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapResult)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return bitmapResult
    }

    private fun resetList() {
        recyclerViewArrayList = WalletHeterogeneousArray(TotalBalance(WalletState.UPDATING,"Refreshing Wallet", "","Updating..."),
                AvailableBalance("XLM", "-1"), Pair("Activity", "Amount"), arrayListOf())
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
        when(newState) {
            WalletState.ACTIVE -> {
                noTransactionsTextView.visibility = View.GONE
                if (viewState != null) {
                    doAsync {
                        //indexes in recycler list are messed up, lets create array again
                        val adapter = createAdapter()
                        updateListData(viewState.effectList!!, viewState.activeAssetCode, viewState.availableBalance!!, viewState.totalBalance!!)
                        uiThread {
                            activity?.let { that ->
                                if (!that.isFinishing && fetchingState != null) {
                                    fetchingState.visibility = View.GONE
                                    fundingState.visibility = View.GONE

                                    walletRecyclerView.adapter = adapter
                                    swipeRefresh_wallet.isRefreshing = false
                                }
                            }
                        }
                    }
                }
            }
            WalletState.UPDATING -> {
                //we had to hide the list of effects :(
                recyclerViewArrayList.updateTotalBalance(TotalBalance(newState,"Refreshing Wallet", "","Updating..."))
                recyclerViewArrayList.hidePair()
                recyclerViewArrayList.hideAvailableBalance()
                val adapter = createAdapter()
                runOnUiThread {
                    activity?.let {
                        if (!it.isFinishing && walletRecyclerView != null) {
                            walletRecyclerView.adapter = adapter
                        }
                    }
                }
            }
            WalletState.ERROR -> {
                recyclerViewArrayList.updateTotalBalance(TotalBalance(newState, "Error fetching from Horizon", "", "Error"))
                recyclerViewArrayList.hidePair()
            }
            WalletState.NOT_FUNDED -> {
                if (viewState != null) {
                   generateQRCode(viewState.accountId, qrCode, 500)
               }

               recyclerViewArrayList.updateTotalBalance(TotalBalance(newState, "Account Funding Required", "", "0.00"))
               recyclerViewArrayList.hideAvailableBalance()
               recyclerViewArrayList.hidePair()
            } else -> {
                //nothing
            }
        }
        runOnUiThread {
            walletRecyclerView.adapter?.notifyDataSetChanged()
            updatePlaceHolders(newState)
        }
        state = newState
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
                        receiveButton.isEnabled = false
                        swipeRefresh_wallet.isRefreshing = false
                        fetchingState.visibility = View.VISIBLE
                        fundingState.visibility = View.GONE
                    } WalletState.ACTIVE -> {
                        sendButton.isEnabled = true
                        receiveButton.isEnabled = true
                        noTransactionsTextView.visibility = View.GONE
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
    private fun createAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        resetList()
        val adapter = WalletRecyclerViewAdapter(activity!!, recyclerViewArrayList.array)
        adapter.setOnAssetDropdownListener(object : WalletRecyclerViewAdapter.OnAssetDropdownListener {
            override fun onAssetDropdownClicked(view: View, position: Int) {
                val context = view.context
                startActivity(Intent(context, AssetsActivity::class.java))
                (context as Activity).overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        })
        adapter.setOnLearnMoreButtonListener(object : WalletRecyclerViewAdapter.OnLearnMoreButtonListener {
            override fun onLearnMoreButtonClicked(view: View, position: Int) {
                val context = view.context
                startActivity(Intent(context, BalanceSummaryActivity::class.java))
                (context as Activity).overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        })
        recyclerViewArrayList.hideAvailableBalance()
        return adapter
    }

    private fun updateListData(list : ArrayList<EffectResponse>, activeAsset : String, availableBalance: AvailableBalance, totalAssetBalance : TotalBalance){
        recyclerViewArrayList.showAvailableBalance(availableBalance)
        recyclerViewArrayList.updateTotalBalance(totalAssetBalance)
        recyclerViewArrayList.updateEffectsList(activeAsset, list)
        recyclerViewArrayList.updateAvailableBalance(availableBalance)
    }

    //endregion

}
