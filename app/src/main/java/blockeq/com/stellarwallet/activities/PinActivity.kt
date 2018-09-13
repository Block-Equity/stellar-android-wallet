package blockeq.com.stellarwallet.activities

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.encryption.CipherWrapper
import blockeq.com.stellarwallet.encryption.KeyStoreWrapper
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.helpers.SupportedMnemonic
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState
import com.andrognito.pinlockview.PinLockListener
import kotlinx.android.synthetic.main.activity_pin.*
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse

class PinActivity : AppCompatActivity(), PinLockListener {

    companion object {
        const val PIN_REQUEST_CODE = 0
        const val RESULT_FAIL = 2

        const val PROD_SERVER = "https://horizon.stellar.org"
        const val TEST_SERVER = "https://horizon-testnet.stellar.org"

        const val USER_INDEX = 0

        const val MAX_ATTEMPTS = 3
        private val TAG = PinActivity::class.java.simpleName

        private class GenerateStellarAddressTask : AsyncTask<String, Void, KeyPair>() {
            override fun doInBackground(vararg mnemonic: String) : KeyPair? {
                val keyPair = SupportedMnemonic.createKeyPair(mnemonic[0].toCharArray(), null, USER_INDEX)
                WalletApplication.localStore!!.publicKey = keyPair.accountId

                return keyPair
            }

            override fun onPostExecute(keyPair: KeyPair?) {
                if (keyPair != null) {
                    LoadAccountTask().execute(keyPair)
                }
            }
        }

        private class LoadAccountTask : AsyncTask<KeyPair, Void, AccountResponse>() {
            override fun doInBackground(vararg pair: KeyPair) : AccountResponse? {
                val server = Server(PROD_SERVER)
                var account : AccountResponse? = null
                try {
                    account = server.accounts().account(pair[0])

                } catch (error : ErrorResponse) {
                    Log.d(TAG, error.body.toString())
                }

                return account
            }

            override fun onPostExecute(result: AccountResponse?) {
                if (result != null) {
                    WalletApplication.localStore!!.balances = result.balances
                }
            }
        }
    }

    private var needConfirm = true
    private var PIN : String? = null
    private var phrase : String? = null
    private var pinViewState: PinViewState? = null
    private var numAttempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        pinLockView.setPinLockListener(this)
        pinLockView.attachIndicatorDots(indicatorDots)

        pinViewState = getPinState()
        phrase = pinViewState!!.phrase
        val message = pinViewState!!.message
        PIN = pinViewState!!.pin

        if (!message.isEmpty()) {
            tv_custom_message.text = message
        }
    }

    override fun onEmpty() {
    }

    override fun onComplete(pin: String) {
        when (pinViewState!!.type) {
            PinType.CREATE -> {
                when {
                    needConfirm -> {
                        PIN = pin
                        pinLockView.resetPinLockView()

                        tv_custom_message.text = getString(R.string.please_reenter_your_pin)
                        needConfirm = false
                    }
                    pin != PIN -> onIncorrectPin()
                    else -> {
                        setResult(Activity.RESULT_OK)

                        val keyStoreWrapper = KeyStoreWrapper(applicationContext)
                        keyStoreWrapper.createAndroidKeyStoreAsymmetricKey(pin)

                        val masterKey = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin)
                        val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")

                        val encryptedData = cipherWrapper.encrypt(pinViewState!!.phrase, masterKey?.public)

                        WalletApplication.localStore!!.encryptedPhrase = encryptedData
                        GenerateStellarAddressTask().execute(pinViewState!!.phrase)

                        launchWallet()
                    }
                }
            }
            PinType.CHECK -> {
                val encryptedPhrase = pinViewState!!.phrase
                val keyStoreWrapper = KeyStoreWrapper(applicationContext)

                val masterKey = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin)
                if (masterKey == null) {
                    onIncorrectPin()
                } else {
                    val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")
                    val decryptedData = cipherWrapper.decrypt(encryptedPhrase, masterKey.private)

                    GenerateStellarAddressTask().execute(decryptedData)

                    launchWallet()
                }
            }
        }
    }

    override fun onPinChange(pinLength: Int, intermediatePin: String?) {
    }

    private fun onIncorrectPin() {
        showWrongPinDots(true)
        val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
        shakeAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                showWrongPinDots(false)
                pinLockView.resetPinLockView()
                numAttempts++
                if (pinViewState!!.type == PinType.CHECK && numAttempts == MAX_ATTEMPTS) {
                    setResult(RESULT_FAIL)
                    finishActivity()
                }
            }
        })
        wrongPinDots.startAnimation(shakeAnimation)
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
    }

    private fun showWrongPinDots(show: Boolean) {
        indicatorDots.visibility = if (show) View.GONE else View.VISIBLE
        wrongPinDots.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
    }

    private fun getPinState(): PinViewState {
        val intent = intent
        val bundle = intent.extras

        return bundle.getParcelable(PinFlowController.OBJECT)
    }

    private fun launchWallet() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}
