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
class WalletFragment : BaseFragment() {

    private lateinit var recyclerViewArrayList: WalletHeterogeneousArray
    private lateinit var viewModel : WalletViewModel
    private var state = WalletState.UNKNOWN

    companion object {
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

        updateState(WalletState.UPDATING)
        initViewModels()

        swipeRefresh_wallet.setOnRefreshListener {
            updateState(WalletState.UPDATING)
            swipeRefresh_wallet.postDelayed({
                activity?.let {
                    if (!it.isFinishing) {
                        viewModel.forceRefresh()
                    }
                }
            }, 400)
        }

        receiveButton.setOnClickListener {
            activity?.let { activityContext ->
                startActivity(Intent(activityContext, ReceiveActivity::class.java))
                activityContext.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
        }

        sendButton.setOnClickListener {
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

    override fun onResume() {
        super.onResume()
        //TODO review this
//        updateAdapter()
    }

    //region User Interface

    private fun initViewModels() {
        viewModel.walletViewState().observe(this, Observer {
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
        if (state == newState) {
            Timber.d("new state ignored because it is the same")
            return
        }
        when(newState) {
            WalletState.ACTIVE -> {
                noTransactionsTextView.visibility = View.GONE
                //indexes in recycler list are messed up, lets create array again
                createAdapter()
                if (viewState != null) {
                    doAsync {
                        updateListData(viewState.effectList!!, viewState.activeAssetCode, viewState.availableBalance!!, viewState.totalBalance!!)
                        uiThread {
                            if (swipeRefresh_wallet != null) {
                                fetchingState.visibility = View.GONE
                                fundingState.visibility = View.GONE

                                walletRecyclerView.adapter?.notifyDataSetChanged()

                                swipeRefresh_wallet.isRefreshing = false
                            }
                        }
                    }
                }
            }
            WalletState.UPDATING -> {
                //we had to hide the list of effects :(
                createAdapter()
                recyclerViewArrayList.updateTotalBalance(TotalBalance(newState,"Refreshing Wallet", "","Updating..."))
                recyclerViewArrayList.hidePair()
                recyclerViewArrayList.hideAvailableBalance()
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
                        swipeRefresh_wallet.isEnabled = true
                        receiveButton.isEnabled = true
                        noTransactionsTextView.visibility = View.GONE
                        fetchingState.visibility = View.GONE
                        fundingState.visibility = View.VISIBLE
                    }
                    WalletState.ERROR -> {
                        noTransactionsTextView.visibility = View.GONE
                        swipeRefresh_wallet.isEnabled = true
                        sendButton.isEnabled = false
                        receiveButton.isEnabled = false
                        fetchingState.visibility = View.VISIBLE
                        fundingState.visibility = View.GONE
                    }
                    WalletState.UPDATING -> {
                        noTransactionsTextView.visibility = View.GONE
                        swipeRefresh_wallet.isEnabled = false
                        sendButton.isEnabled = false
                        receiveButton.isEnabled = true
                        swipeRefresh_wallet.isRefreshing = false
                        fetchingState.visibility = View.VISIBLE
                        fundingState.visibility = View.GONE
                    } WalletState.ACTIVE -> {
                        noTransactionsTextView.visibility = View.GONE
                        swipeRefresh_wallet.isEnabled = true
                    } else -> {
                        // nothing
                    }
                }
            }
        }
    }

    private fun createAdapter() {
        recyclerViewArrayList = WalletHeterogeneousArray(TotalBalance(WalletState.UPDATING,"Refreshing Wallet", "","Updating..."),
                AvailableBalance("XLM", "-1"), Pair("Activity", "Amount"), arrayListOf())

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
        walletRecyclerView.adapter = adapter
        walletRecyclerView.layoutManager = LinearLayoutManager(activity)

        fetchingState.visibility = View.VISIBLE
        fundingState.visibility = View.GONE
    }

    private fun updateListData(list : ArrayList<EffectResponse>, activeAsset : String, availableBalance: AvailableBalance, totalAssetBalance : TotalBalance){
        recyclerViewArrayList.showAvailableBalance(availableBalance)
        recyclerViewArrayList.updateTotalBalance(totalAssetBalance)
        recyclerViewArrayList.updateEffectsList(activeAsset, list)
        recyclerViewArrayList.updateAvailableBalance(availableBalance)
    }

    //endregion

    //region Call backs
//TODO: propagate errors to ui?
//    override fun onError(error: ErrorResponse) {
//        if (error.code == Constants.SERVER_ERROR_NOT_FOUND && walletProgressBar != null) {
//            Handler(Looper.getMainLooper()).post {
//                if (noTransactionsTextView != null) {
//                    noTransactionsTextView.visibility = View.VISIBLE
//                }
//            }
//        }
//    }

    //endregion

}
