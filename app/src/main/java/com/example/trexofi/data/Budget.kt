package com.example.trexofi.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val month: Int,
    val year: Int,
    val category: String? = null,
    val startDate: Date,
    val endDate: Date
) 