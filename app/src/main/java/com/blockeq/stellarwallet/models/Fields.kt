package com.blockeq.stellarwallet.models

import org.json.JSONObject

data class Fields(val app_version: String, val device_hardware: String, val locale: String,
                  val platform: String, val public_wallet_address: String, val summary : String,
                  val used_passphrase: Boolean, val wallet_creation_method: String) {

   fun toJSON () : JSONObject {
       val fields = JSONObject()

       fields.put("App Version", app_version)
       fields.put("Device Hardware", device_hardware)
       fields.put("Locale", locale)
       fields.put("Platform", platform)
       fields.put("Public Wallet Address", public_wallet_address)
       fields.put("Summary", summary)
       fields.put("Used Passphrase", used_passphrase)
       fields.put("Wallet Creation Method", wallet_creation_method)

       return fields
   }
}
