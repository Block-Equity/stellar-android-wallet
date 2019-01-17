package com.blockeq.stellarwallet.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.View
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.fragments.ContactsFragment
import com.blockeq.stellarwallet.fragments.SettingsFragment
import com.blockeq.stellarwallet.fragments.TradingFragment
import com.blockeq.stellarwallet.fragments.WalletFragment
import com.blockeq.stellarwallet.mvvm.effects.WalletViewModel
import com.blockeq.stellarwallet.utils.KeyboardUtils
import org.stellar.sdk.responses.AccountResponse

class WalletActivity : BaseActivity(), KeyboardUtils.SoftKeyboardToggleListener {
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

        ViewModelProviders.of(this).get(WalletViewModel::class.java)
                .account.observe(this, Observer<AccountResponse> {
                    if (it != null) {
                        bottomNavigation.menu.getItem(WalletFragmentType.TRADING.ordinal).isEnabled = true
                    }
                })
    }


    //endregion

    override fun onResume() {
        super.onResume()

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

//        }
    //TODO: move onError to viewModel
//    override fun onError(error: ErrorResponse) {
//        val fragment = supportFragmentManager.findFragmentByTag(WalletFragmentType.WALLET.name)
//        if (fragment != null) {
//            (fragment as WalletFragment).onError(error)
//    }
}
