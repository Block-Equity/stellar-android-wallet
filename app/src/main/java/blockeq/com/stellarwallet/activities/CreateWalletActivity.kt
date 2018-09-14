package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.activities.PinActivity.Companion.PIN_REQUEST_CODE
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState
import com.soneso.stellarmnemonics.Wallet
import kotlinx.android.synthetic.main.activity_create_wallet.*


class CreateWalletActivity : BaseActivity(), View.OnClickListener {

    private var mnemonicString : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wallet)

        setupUI()
        setOnClickListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIN_REQUEST_CODE) {
            finish()
        }
    }

    //region User Interface
    override fun onClick(v: View?) {
        val itemId = v!!.id
        when (itemId) {
            R.id.confirmButton -> launchPINView()
        }
    }

    override fun setupUI() {
        setupActionBar()
        setupMnemonicView()
    }

    private fun setupActionBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupMnemonicView() {
        mnemonicView.mnemonic = getMnemonic()
        mnemonicView.loadMnemonic()
    }

    //endregion

    //region Set onClick interfaces
    private fun setOnClickListeners() {
        confirmButton.setOnClickListener(this)
    }
    //endregion

    //region Helper functions
    private fun getMnemonic(): ArrayList<String> {
        val mnemonic = if (intent.getIntExtra("walletLength", 12) == 12) {
            Wallet.generate12WordMnemonic()
        } else {
            Wallet.generate24WordMnemonic()
        }

        mnemonicString = String(mnemonic)
        val words = String(mnemonic).split(" ".toRegex()).dropLastWhile { it.isEmpty() } as ArrayList
        return words
    }

    private fun launchPINView() {
        PinFlowController.launchPinActivity(this, getPinViewState())
    }

    private fun getPinViewState(): PinViewState {
        return PinViewState(PinType.CREATE, getString(R.string.please_create_a_pin), "", mnemonicString!!)
    }
    //endregion
}
