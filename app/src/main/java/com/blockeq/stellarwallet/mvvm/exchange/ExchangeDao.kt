package com.blockeq.stellarwallet.mvvm.exchange

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface ExchangeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(exchange: ExchangeEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<ExchangeEntity>)

    @Query("DELETE FROM exchanges")
    fun deleteAll()

    @Query("SELECT * FROM exchanges" )
    fun getAllExchangeProviders() : List<ExchangeEntity>


    @Query("SELECT * FROM exchanges WHERE address LIKE :address LIMIT 1" )
    fun getExchangeProvider(address: String) : ExchangeEntity

}