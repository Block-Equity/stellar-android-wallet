package com.blockeq.stellarwallet.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.blockeq.stellarwallet.R
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity() {

    companion object {
        private const val URL = "URL"

        fun newWebsiteIntent(context: Context?, url : String): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(URL, url)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        webview.loadUrl(intent.getStringExtra(URL))
    }
}
