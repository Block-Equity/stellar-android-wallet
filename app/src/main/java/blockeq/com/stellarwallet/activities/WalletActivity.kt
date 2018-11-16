package blockeq.com.stellarwallet.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.fragments.SettingsFragment
import blockeq.com.stellarwallet.fragments.TradingFragment
import blockeq.com.stellarwallet.fragments.WalletFragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class WalletActivity : BaseActivity() {
    private lateinit var newAppDialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        setupUI()

        checkNewApp()
    }


    private fun checkNewApp() {
        val builder = AlertDialog.Builder(this@WalletActivity)
        builder.setTitle("The Beta program has ended")
        builder.setMessage("The current app is no longer maintained. Please make sure you have written down your recovery phrase (12-24 words) or saved a copy of your secret key. After you have saved a back-up, please delete this app and then recover your wallet using the new version here")
        builder.setPositiveButton("Download") { _, _ ->
            val appPackageName = packageName // getPackageName() from Context or Activity object
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")));
            } catch (e : ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")));
            }
        }
        builder.setNegativeButton("Later", null)
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(Request.Method.GET, "https://play.google.com/store/apps/details?id=blockeq.com.stellarwallet", Response.Listener<String> {
            if (!isFinishing) {
                newAppDialog.show()
            }
        }, null)

        newAppDialog = builder.create()
        queue.add(request)
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

    private fun setupUI() {
        setupNav()
    }

    private fun setupNav() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
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
}
