package com.example.cashmanager.data

import androidx.room.*
import com.example.cashmanager.data.models.SendTran
import io.reactivex.Completable

@Dao
interface SendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransfer(transfer: SendTran): Completable

    @Delete
    fun removeTransfer(transfer: SendTran): Completable

    @Query("SELECT * FROM user_transfers WHERE email = :emailAddress")
    fun getTransferByEmail(emailAddress: String): SendTran

    @Query("SELECT * FROM user_transfers")
    fun getAll(): List<SendTran>

}