package com.blockeq.stellarwallet.models

import android.net.Uri

/**
 * Entity that represents a contact
 */
data class Contact(var id: Long,  var name: String?, var profilePic: Uri?, var stellarAddress: String?)
