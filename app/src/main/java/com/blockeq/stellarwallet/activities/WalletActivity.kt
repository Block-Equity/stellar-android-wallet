package com.blockeq.stellarwallet.activities

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.View
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.fragments.ContactsFragment
import com.blockeq.stellarwallet.fragments.SettingsFragment
import com.blockeq.stellarwallet.fragments.TradingFragment
import com.blockeq.stellarwallet.fragments.WalletFragment
import com.blockeq.stellarwallet.interfaces.OnLoadAccount
import com.blockeq.stellarwallet.interfaces.OnLoadEffects
import com.blockeq.stellarwallet.models.MinimumBalance
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.KeyboardUtils
import com.blockeq.stellarwallet.utils.NetworkUtils
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse
import java.util.*

class WalletActivity : BaseActivity(), OnLoadAccount, OnLoadEffects, KeyboardUtils.SoftKeyboardToggleListener {
    private enum class WalletFragmentType {
        WALLET,
        TRADING,
        CONTACTS,
        SETTING
    }

    private lateinit var dialogTradeAlert : Dialog
    private lateinit var bottomNavigation : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        dialogTradeAlert = createTradingErrorDialog()

        setupUI()

    }

    //region Navigation

    private fun createTradingErrorDialog() : Dialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.trade_alert_title))
        builder.setMessage(getString(R.string.trade_alert_message))
        builder.setPositiveButton(getString(R.string.trade_alert_button)) { _, _ -> startActivity(AssetsActivity.newInstance(this)) }
        val dialog = builder.create()
        dialog.setOnCancelListener {
            bottomNavigation.selectedItemId = R.id.nav_wallet
        }
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_wallet -> {
                val walletFragment = WalletFragment.newInstance()
                openFragment(walletFragment, WalletFragmentType.WALLET)
            }
            R.id.nav_trading -> {
                val balances = WalletApplication.wallet.getBalances()
                if (balances.isEmpty()) {
                    dialogTradeAlert.show()
                }
                val tradingFragment = TradingFragment.newInstance()
                openFragment(tradingFragment, WalletFragmentType.TRADING)
            }
            R.id.nav_contacts -> {
                openFragment(ContactsFragment(), WalletFragmentType.CONTACTS)
            }
            R.id.nav_settings -> {
                val settingsFragment = SettingsFragment.newInstance()
                openFragment(settingsFragment, WalletFragmentType.SETTING)
            }
            else -> throw IllegalAccessException("Navigation item not supported $item.title(${item.itemId})")
        }
        return@OnNavigationItemSelectedListener true
    }

    private fun openFragment(fragment: Fragment, type : WalletFragmentType) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content_container, fragment, type.name)
        transaction.commit()
    }

    private fun setupUI() {
        bottomNavigation = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottomNavigation.selectedItemId = R.id.nav_wallet
    }

    //endregion

    override fun onResume() {
        super.onResume()
        startPollingAccount()

        if (bottomNavigation.selectedItemId ==  R.id.nav_trading) {
            val balances = WalletApplication.wallet.getBalances()
            if (balances.isEmpty()) {
                dialogTradeAlert.show()
            }
        }

        KeyboardUtils.addKeyboardToggleListener(this, this)
    }

    override fun onPause() {
        super.onPause()
        endPollingAccount()

        KeyboardUtils.removeKeyboardToggleListener(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialogTradeAlert.isShowing) {
            dialogTradeAlert.dismiss()
        }
    }
    /**
     * When the keyboard is opened the bottomNavigation gets pushed up.
     */
    override fun onToggleSoftKeyboard(isVisible: Boolean) {
        if (isVisible) {
            bottomNavigation.visibility = View.GONE
        } else {
            bottomNavigation.visibility = View.VISIBLE
        }
    }

    private var handler = Handler()
    private var runnableCode : Runnable? = null

    //TODO polling for only non-created accounts on Stellar.
    private fun startPollingAccount() {
        runnableCode = object : Runnable {
            override fun run() {

                if (NetworkUtils(applicationContext).isNetworkAvailable()) {

                    Horizon.getLoadAccountTask(this@WalletActivity)
                            .execute()

                    Horizon.getLoadEffectsTask(this@WalletActivity)
                            .execute()
                } else {
                    NetworkUtils(applicationContext).displayNoNetwork()
                }

                handler.postDelayed(this, 5000)
            }
        }

        handler.post(runnableCode)
    }

    private fun endPollingAccount() {
        handler.removeCallbacks(runnableCode)
    }

    override fun onLoadAccount(result: AccountResponse?) {
        if (result != null) {
            WalletApplication.wallet.setBalances(result.balances)
            WalletApplication.userSession.minimumBalance = MinimumBalance(result)
            WalletApplication.wallet.setAvailableBalance(AccountUtils.calculateAvailableBalance())

            bottomNavigation.menu.getItem(WalletFragmentType.TRADING.ordinal).isEnabled = true
        }

        val fragment = supportFragmentManager.findFragmentByTag(WalletFragmentType.WALLET.name)
        if (fragment != null) {
            (fragment as WalletFragment).onLoadAccount(result)
        }
    }

    override fun onError(error: ErrorResponse) {
        val fragment = supportFragmentManager.findFragmentByTag(WalletFragmentType.WALLET.name)
        if (fragment != null) {
            (fragment as WalletFragment).onError(error)
        }
    }

    override fun onLoadEffects(result: ArrayList<EffectResponse>?) {
        val fragment = supportFragmentManager.findFragmentByTag(WalletFragmentType.WALLET.name)
        if (fragment != null) {
            (fragment as WalletFragment).onLoadEffects(result)
        }
    }
}
