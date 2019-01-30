package com.blockeq.stellarwallet.activities

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
import com.blockeq.stellarwallet.utils.KeyboardUtils
import timber.log.Timber

class WalletActivity : BaseActivity(), KeyboardUtils.SoftKeyboardToggleListener {
    private enum class WalletFragmentType {
        WALLET,
        TRADING,
        CONTACTS,
        SETTING
    }

    private lateinit var dialogTradeAlert : Dialog
    private lateinit var bottomNavigation : BottomNavigationView
    private var currentItemSelected : Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        dialogTradeAlert = createTradingErrorDialog()

        setupUI()
    }

    private fun getReusedFragment(tag:String) : Fragment? {
       val fragment = supportFragmentManager.findFragmentByTag(tag)
       if (fragment != null) {
           Timber.d("reused a cached fragment {$tag}")
       }
       return fragment
    }

    //region Navigation

    private fun createTradingErrorDialog() : Dialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.trade_alert_title))
        builder.setMessage(getString(R.string.trade_alert_message))
        builder.setPositiveButton(getString(R.string.trade_alert_positive_button)) { _, _ -> startActivity(AssetsActivity.newInstance(this)) }
        builder.setNegativeButton(getString(R.string.trade_alert_negative_button)) { dialog, _ -> dialog.cancel() }
        val dialog = builder.create()

        dialog.setOnCancelListener {
            bottomNavigation.selectedItemId = R.id.nav_wallet
        }
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        //let's ignore item selection in the current item
        if (currentItemSelected != item.itemId) {
            currentItemSelected = item.itemId
            when (item.itemId) {
                R.id.nav_wallet -> {
                    val walletFragment = getReusedFragment(WalletFragmentType.WALLET.name)
                            ?: WalletFragment.newInstance()
                    replaceFragment(walletFragment, WalletFragmentType.WALLET)
                }
                R.id.nav_trading -> {
                    // minimum two trades
                    if (!enoughAssetsToTrade()) {
                        dialogTradeAlert.show()
                    }
                    val tradingFragment = getReusedFragment(WalletFragmentType.TRADING.name)
                            ?: TradingFragment.newInstance()
                    replaceFragment(tradingFragment, WalletFragmentType.TRADING)
                }
                R.id.nav_contacts -> {
                    replaceFragment(getReusedFragment(WalletFragmentType.CONTACTS.name)
                            ?: ContactsFragment(), WalletFragmentType.CONTACTS)
                }
                R.id.nav_settings -> {
                    val settingsFragment = getReusedFragment(WalletFragmentType.SETTING.name)
                            ?: SettingsFragment.newInstance()
                    replaceFragment(settingsFragment, WalletFragmentType.SETTING)
                }
                else -> throw IllegalAccessException("Navigation item not supported $item.title(${item.itemId})")
            }
        }
        return@OnNavigationItemSelectedListener true
    }

    private fun replaceFragment(fragment: Fragment, type : WalletFragmentType) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content_container, fragment, type.name)
        //This is complete necessary to be able to reuse the fragments using the supportFragmentManager
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setupUI() {
        bottomNavigation = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        bottomNavigation.selectedItemId = R.id.nav_wallet
    }


    //endregion

    override fun onResume() {
        super.onResume()

        if (bottomNavigation.selectedItemId ==  R.id.nav_trading) {
            if (!enoughAssetsToTrade()) {
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

    private fun enoughAssetsToTrade() : Boolean {
        val balances = WalletApplication.wallet.getBalances()
        //minimum 2 assets to trade
        return balances.size > 1
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

    override fun onBackPressed() {
        if (bottomNavigation.selectedItemId != R.id.nav_wallet) {
            bottomNavigation.selectedItemId = R.id.nav_wallet
        } else {
           finish()
        }
    }
}
