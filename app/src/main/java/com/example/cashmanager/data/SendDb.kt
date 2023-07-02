package com.example.cashmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cashmanager.data.models.SendTran

@Database(entities = [SendTran::class], version = 1)
abstract class SendDb: RoomDatabase() {

    companion object {

        private const val DB_NAME_SEND = "USER_TRANSFERS"

        @Volatile
        private var sendInstance: SendDb? = null

        fun getInstance(context: Context): SendDb {
            return sendInstance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SendDb::class.java,
                    DB_NAME_SEND
                )
                    .fallbackToDestructiveMigration()
                    .build()
                sendInstance = instance
                // return instance
                instance
            }

        }
    }

    abstract fun sendDao(): SendDao
}