package com.blockeq.stellarwallet.mvvm.exchange

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.blockeq.stellarwallet.models.ExchangeApiModel
import shadow.com.google.gson.GsonBuilder
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.Executors

@Database(entities = [ExchangeEntity::class], version = 1)
abstract class ExchangesRoomDatabase : RoomDatabase() {

    abstract fun exchangeDao(): ExchangeDao

    companion object {

        private var INSTANCE: ExchangesRoomDatabase? = null

        internal fun getDatabase(context: Context): ExchangesRoomDatabase {
            if (INSTANCE == null) {
                synchronized(ExchangesRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                ExchangesRoomDatabase::class.java, "exchanges_database")
                                .allowMainThreadQueries()
                                .addCallback(object : RoomDatabase.Callback() {
                                    override fun onCreate(db: SupportSQLiteDatabase) {
                                        super.onCreate(db)
                                        val json = readExchangesFromAssets(context.applicationContext)
                                        if (json != null) {
                                            val exchanges = parseJson(json)
                                            val entities = exchanges.map {
                                                ExchangeEntity(it.name, it.address, it.memo)
                                            }

                                            populateDatabase(context.applicationContext, entities)
                                            Timber.d("Populating the empty database with a local resource")
                                        }
                                    }
                                }).build()
                    }
                }
            }
            return INSTANCE as ExchangesRoomDatabase
        }

        private fun parseJson(input : String) : List<ExchangeApiModel> {
            val gson = GsonBuilder().create()
            val list = gson.fromJson(input, Array<ExchangeApiModel>::class.java)
            return list.toList()
        }

        private fun readExchangesFromAssets(context: Context): String? {
            var json: String? = null
            try {
                val inputStream = context.assets.open("exchanges.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                json = String(buffer, Charsets.UTF_8)
            } catch (ex: IOException) {
                Timber.e("error reading the exchanges addresses:$ex")
            }

            return json
        }

        private fun populateDatabase(context: Context, exchanges : List<ExchangeEntity>) {
            Executors.newSingleThreadScheduledExecutor().execute {
                val database = getDatabase(context)
                database.exchangeDao().insertAll(exchanges)
                Timber.d("Populating exchange database from local resource")
            }
        }
    }
}