package blockeq.com.stellarwallet.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.encryption.CipherWrapper
import blockeq.com.stellarwallet.encryption.KeyStoreWrapper
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.helpers.StellarAddress
import blockeq.com.stellarwallet.interfaces.OnLoadAccount
import blockeq.com.stellarwallet.interfaces.OnWalletSeedCreated
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState
import blockeq.com.stellarwallet.services.networking.Horizon
import com.andrognito.pinlockview.PinLockListener
import kotlinx.android.synthetic.main.activity_pin.*
import org.stellar.sdk.KeyPair
import org.stellar.sdk.responses.AccountResponse

class PinActivity : BaseActivity(), PinLockListener, OnWalletSeedCreated, OnLoadAccount {

    companion object {
        const val PIN_REQUEST_CODE = 0
        const val RESULT_FAIL = 2

        const val MAX_ATTEMPTS = 3
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
                        StellarAddress.Companion.Generate(this).execute(pinViewState!!.phrase)

                        launchWallet()
                    }
                }
            }
            else -> {
                val encryptedPhrase = pinViewState!!.phrase
                val masterKey = isCorrectPinMasterKey(pin)

                if (masterKey != null) {
                    val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")
                    val decryptedData = cipherWrapper.decrypt(encryptedPhrase, masterKey.private)

                    when {
                        pinViewState!!.type == PinType.CHECK -> {
                            StellarAddress.Companion.Generate(this).execute(decryptedData)
                            launchWallet()
                        }
                        pinViewState!!.type == PinType.CLEAR_WALLET -> wipeAndRestart()

                        pinViewState!!.type == PinType.VIEW_PHRASE -> {
                            val intent = Intent(this, ShowMnemonicActivity::class.java)
                            intent.putExtra(ShowMnemonicActivity.INTENT_DISPLAY_PHRASE, true)
                            intent.putExtra(ShowMnemonicActivity.DECRYPTED_PHRASE, decryptedData)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    onIncorrectPin()
                }
            }
        }
    }

    //region User Interface

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
                if (numAttempts == MAX_ATTEMPTS) {

                    if (pinViewState!!.type == PinType.CHECK) {
                        setResult(RESULT_FAIL)
                        finishActivity()
                    } else {
                        wipeAndRestart()
                    }
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

    //endregion


    //region Call backs

    override fun onWalletSeedCreated(keyPair : KeyPair?) {
        if (keyPair != null) {
            Horizon.Companion.LoadAccountTask(this).execute(keyPair)
            Horizon.Companion.LoadEffectsTask().execute(keyPair)
        }
    }

    override fun onLoadAccount(result: AccountResponse?) {
        if (result != null) {
            WalletApplication.localStore!!.balances = result.balances
        }
    }

    //endregion

    //region Encryption and Decryption
    private fun isCorrectPinMasterKey(pin: String) : java.security.KeyPair? {
        val keyStoreWrapper = KeyStoreWrapper(applicationContext)

        return keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(pin)
    }

    private fun wipeAndRestart() {
        WalletApplication.localStore!!.clearUserData()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
    //endregion
}
