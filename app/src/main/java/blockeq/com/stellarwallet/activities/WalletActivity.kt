package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.fragments.SettingsFragment
import blockeq.com.stellarwallet.fragments.TradingFragment
import blockeq.com.stellarwallet.fragments.WalletFragment
import blockeq.com.stellarwallet.interfaces.OnLoadAccount
import blockeq.com.stellarwallet.interfaces.OnLoadEffects
import blockeq.com.stellarwallet.models.GooglePlayApp
import blockeq.com.stellarwallet.models.MinimumBalance
import blockeq.com.stellarwallet.services.networking.Horizon
import blockeq.com.stellarwallet.utils.AccountUtils
import blockeq.com.stellarwallet.utils.NetworkUtils
import blockeq.com.stellarwallet.utils.UpdateAppDialog
import blockeq.com.stellarwallet.utils.UpdateAppDialog.NEW_APP_PACKAGE
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse
import java.util.ArrayList

class WalletActivity : BaseActivity(), OnLoadAccount, OnLoadEffects {
    private enum class WalletFragmentType {
        WALLET,
        TRADING,
        SETTING
    }

    private lateinit var newAppDialog : AlertDialog
    private lateinit var bottomNavigation : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        setupUI()

        if (packageName != NEW_APP_PACKAGE) {
            newAppDialog = UpdateAppDialog.createDialog(this, GooglePlayApp(NEW_APP_PACKAGE), getString(R.string.update_app_dialog_message_2))
        }
    }

    //region Navigation

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_wallet -> {
                val walletFragment = WalletFragment.newInstance()
                openFragment(walletFragment, WalletFragmentType.WALLET)
            }
            R.id.nav_trading -> {
                val balances = WalletApplication.localStore.balances
                if (balances == null || balances.isEmpty()) {

                } else {
                    val tradingFragment = TradingFragment.newInstance()
                    openFragment(tradingFragment, WalletFragmentType.TRADING)
                }
            }

            R.id.nav_settings -> {
                val settingsFragment = SettingsFragment.newInstance()
                openFragment(settingsFragment, WalletFragmentType.SETTING)
            }
            else -> throw IllegalAccessException("navigation item not supported $item.title(${item.itemId})")
        }
        return@OnNavigationItemSelectedListener true
    }

    private fun openFragment(fragment: Fragment, type : WalletFragmentType) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment, type.name)
        transaction.commit()
    }

    private fun setupUI() {
        bottomNavigation = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottomNavigation.selectedItemId = R.id.nav_wallet
    }

    //endregion

    override fun onDestroy() {
        super.onDestroy()
        if (newAppDialog.isShowing) {
            newAppDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        startPollingAccount()
    }

    override fun onPause() {
        super.onPause()
        endPollingAccount()
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
            WalletApplication.localStore.balances = result.balances
            WalletApplication.userSession.minimumBalance = MinimumBalance(result)
            WalletApplication.localStore.availableBalance = AccountUtils.calculateAvailableBalance()

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
