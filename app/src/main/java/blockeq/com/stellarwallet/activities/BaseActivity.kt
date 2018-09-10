package blockeq.com.stellarwallet.activities

import android.support.v7.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    open fun setupUI() {
        throw IllegalStateException("Please override this function.")
    }

}