package com.blockeq.stellarwallet.models

import android.net.Uri

/**
 * Entity that represents a contact
 */
data class Contact(var id: Long,  var name: String?, var profilePic: Uri?) {
    /**
     * this is not part of the default constructor to prevent its usage in the generated equals() / hashCode()
     */
    var stellarAddress : String? = null
}
