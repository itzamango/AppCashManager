package com.example.cashmanager.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "currency_transfers", indices = [Index(value = ["id"], unique = true)])
data class CurrencyTransfer(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @ColumnInfo(name = "type")
    val type: String?,

    @ColumnInfo(name = "amount")
    val amount: String?,

    @ColumnInfo(name = "from_to_user")
    val userFromTo: String,

    @ColumnInfo(name = "asset")
    val asset: String,

    @ColumnInfo(name = "createdAt")
    val createdAt: String,

    @ColumnInfo(name = "status")
    val status: String

)