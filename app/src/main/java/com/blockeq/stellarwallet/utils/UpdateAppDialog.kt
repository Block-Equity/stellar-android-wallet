package com.blockeq.stellarwallet.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.models.GooglePlayApp
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

object UpdateAppDialog {
     const val NEW_APP_PACKAGE : String = "com.blockeq.stellarwallet"

     fun createDialog(activity : Activity, app : GooglePlayApp, message : String) : AlertDialog {
        val alertDialog : AlertDialog
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.update_app_dialog_title))
        builder.setMessage(message)
        builder.setPositiveButton(activity.getString(R.string.update_app_dialog_positive_button)) { _, _ ->
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.getDeepLink())))
            } catch (e : ActivityNotFoundException) {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.getUrl())))
            }
        }
        builder.setNegativeButton(activity.getString(R.string.update_app_dialog_negative_button), null)
        val queue = Volley.newRequestQueue(activity.applicationContext)
         alertDialog = builder.create()
        val request = StringRequest(Request.Method.GET, app.getUrl(), Response.Listener<String> {
            if (!activity.isFinishing) {
                alertDialog.show()
            }
        }, null)

        queue.add(request)
        return alertDialog
    }
}
