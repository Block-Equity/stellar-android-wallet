package blockeq.com.stellarwallet.models

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import android.util.Log
import blockeq.com.stellarwallet.helpers.Constants
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import java.io.IOException
import java.nio.charset.Charset

class SendingViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private var context: Context = application.applicationContext
    private var exchangeList : List<ExchangeProvider>? = null

    init {
        loadJSONFromAsset()
    }

    fun getExchangeProvider(address: String): ExchangeProvider? {
        exchangeList.let {
            return it?.find { provider -> provider.address == address  }
        }
    }

    private fun loadExchangeProviderAddresses() {
        val queue = Volley.newRequestQueue(context)

        // TODO: Use retrofit and dagger
        val request = JsonArrayRequest(Request.Method.GET, Constants.BLOCKEQ_EXCHANGES_URL, null,
                Response.Listener { response ->
                    // display response
                    exchangeList = parseExchangeProviders(response.toString())
                },
                Response.ErrorListener {
                    it.networkResponse
                    Log.e("error", "error loading exchange providers")
                })

        queue.add(request)
    }

    private fun parseExchangeProviders(json : String) : List<ExchangeProvider> {
        val gson = GsonBuilder().create()
        val list = gson.fromJson(json, Array<ExchangeProvider>::class.java)
        return list.toList()
    }

    private fun loadJSONFromAsset(): String? {
        var json: String? = null
        try {
            val inputStrem = context.assets.open("exchanges.json")
            val size = inputStrem.available()
            val buffer = ByteArray(size)
            inputStrem.read(buffer)
            inputStrem.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return json
    }
}