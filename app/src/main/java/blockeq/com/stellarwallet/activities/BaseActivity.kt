package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
    }

    open fun setupUI() {
        throw IllegalStateException("Please override this function.")
    }

}