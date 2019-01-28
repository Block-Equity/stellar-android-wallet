package com.blockeq.stellarwallet.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

/**
 * Entity that represents a contact
 */
data class Contact(var id: Long, var name: String, var profilePic: Uri?) : Parcelable {
    /**
     * this is not part of the default constructor to prevent its usage in the generated equals() / hashCode()
     */
    var stellarAddress : String? = null

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readParcelable(Uri::class.java.classLoader)) {
        stellarAddress = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeParcelable(profilePic, flags)
        parcel.writeString(stellarAddress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Contact> {
        override fun createFromParcel(parcel: Parcel): Contact {
            return Contact(parcel)
        }

        override fun newArray(size: Int): Array<Contact?> {
            return arrayOfNulls(size)
        }
    }

}
