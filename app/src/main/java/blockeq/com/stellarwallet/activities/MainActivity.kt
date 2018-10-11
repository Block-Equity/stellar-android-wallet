package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.fragments.SettingsFragment
import blockeq.com.stellarwallet.fragments.WalletFragment
import blockeq.com.stellarwallet.helpers.disableShiftMode

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()
    }

    //region Navigation

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_wallet -> {
                val walletFragment = WalletFragment.newInstance()
                openFragment(walletFragment)
                return@OnNavigationItemSelectedListener true
            }
//            R.id.nav_trading -> {
//                val tradingFragment = TradingFragment.newInstance()
//                openFragment(tradingFragment)
//                return@OnNavigationItemSelectedListener true
//            }
            R.id.nav_settings -> {
                val settingsFragment = SettingsFragment.newInstance()
                openFragment(settingsFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    //endregion

    override fun setupUI() {
        setupNav()
    }

    private fun setupNav() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.disableShiftMode()
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottomNavigation.selectedItemId = R.id.nav_wallet
    }
    //endregion
}
