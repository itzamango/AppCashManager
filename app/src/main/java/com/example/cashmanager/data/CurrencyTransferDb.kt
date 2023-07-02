package com.example.cashmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cashmanager.data.models.CurrencyTransfer

@Database(entities = [CurrencyTransfer::class], version = 1)
abstract class CurrencyTransferDb: RoomDatabase() {
    abstract fun currencyTransferDao(): CurrencyTransferDao
    companion object {

        private const val DB_NAME_TRANFER = "currency_transfers"

        @Volatile
        private var INSTANCE: CurrencyTransferDb? = null

        fun getDatabase(context: Context): CurrencyTransferDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CurrencyTransferDb::class.java,
                    DB_NAME_TRANFER
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }

}
