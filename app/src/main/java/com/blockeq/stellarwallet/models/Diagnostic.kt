package com.blockeq.stellarwallet.models

import org.json.JSONObject

data class Diagnostic(val fields: Fields) {
    fun toJSON () : JSONObject {
        val json = JSONObject()
        json.put("fields", fields.toJSON())
        return json
    }
}
