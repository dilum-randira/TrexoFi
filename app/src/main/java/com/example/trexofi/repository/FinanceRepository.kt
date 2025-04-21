package com.example.trexofi.repository

import androidx.lifecycle.LiveData
import com.example.trexofi.data.*
import java.util.Date

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    // Transaction operations
    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category)
    }

    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    fun getTotalAmountByType(type: TransactionType, startDate: Date, endDate: Date): LiveData<Double?> {
        return transactionDao.getTotalAmountByType(type, startDate, endDate)
    }

    // Budget operations
    fun getBudgetForMonth(month: Int, year: Int): LiveData<List<Budget>> {
        return budgetDao.getBudgetForMonth(month, year)
    }

    fun getCategoryBudget(category: String, month: Int, year: Int): LiveData<Budget?> {
        return budgetDao.getCategoryBudget(category, month, year)
    }

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    fun getTotalBudgetForMonth(month: Int, year: Int): LiveData<Double?> {
        return budgetDao.getTotalBudgetForMonth(month, year)
    }
} 