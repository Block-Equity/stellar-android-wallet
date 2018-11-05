package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.fragments.SettingsFragment
import blockeq.com.stellarwallet.fragments.TradingFragment
import blockeq.com.stellarwallet.fragments.WalletFragment

class WalletActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        setupUI()
    }

    //region Navigation

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_wallet -> {
                val walletFragment = WalletFragment.newInstance()
                openFragment(walletFragment)
            }
            R.id.nav_trading -> {
                val tradingFragment = TradingFragment.newInstance()
                openFragment(tradingFragment)
            }
            R.id.nav_settings -> {
                val settingsFragment = SettingsFragment.newInstance()
                openFragment(settingsFragment)
            }
            else -> throw IllegalAccessException("navigation item not supported $item.title(${item.itemId})")
        }
        return@OnNavigationItemSelectedListener true
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    //endregion

    private fun setupUI() {
        setupNav()
    }

    private fun setupNav() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottomNavigation.selectedItemId = R.id.nav_wallet
    }
    //endregion
}
