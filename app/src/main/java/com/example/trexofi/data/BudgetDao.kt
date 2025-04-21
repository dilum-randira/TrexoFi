package com.example.trexofi.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getBudgetForMonth(month: Int, year: Int): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE category = :category AND month = :month AND year = :year")
    fun getCategoryBudget(category: String, month: Int, year: Int): LiveData<Budget?>

    @Insert
    suspend fun insertBudget(budget: Budget)

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT SUM(amount) FROM budgets WHERE month = :month AND year = :year")
    fun getTotalBudgetForMonth(month: Int, year: Int): LiveData<Double?>
} 