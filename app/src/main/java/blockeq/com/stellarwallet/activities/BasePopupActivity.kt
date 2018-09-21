package blockeq.com.stellarwallet.activities

import android.os.Bundle
import android.view.View
import blockeq.com.stellarwallet.R
import kotlinx.android.synthetic.main.activity_base_popup.*

abstract class BasePopupActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_popup)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        titleText.setText(setTitle())
        View.inflate(this, setContent(), container)
        cancelButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
        }
    }

    abstract fun setTitle() : Int
    abstract fun setContent() : Int

}