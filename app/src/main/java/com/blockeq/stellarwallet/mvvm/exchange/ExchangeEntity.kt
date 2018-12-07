package com.blockeq.stellarwallet.mvvm.exchange

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName="exchanges")
data class ExchangeEntity(val name:String, val address:String, val memo:String) {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
}
