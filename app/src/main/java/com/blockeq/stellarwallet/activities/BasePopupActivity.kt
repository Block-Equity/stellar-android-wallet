package com.blockeq.stellarwallet.activities

import android.os.Bundle
import android.view.View
import com.blockeq.stellarwallet.R
import kotlinx.android.synthetic.main.activity_base_popup.*

abstract class BasePopupActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_popup)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        View.inflate(this, setContent(), content_container)
        cancelButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
        }
    }

    abstract fun setContent() : Int

}
