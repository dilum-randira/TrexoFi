package com.example.trexofi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.trexofi.data.*
import com.example.trexofi.repository.FinanceRepository
import kotlinx.coroutines.launch
import java.util.*

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository
    val allTransactions: LiveData<List<Transaction>>

    init {
        val dao = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(dao.transactionDao(), dao.budgetDao())
        allTransactions = repository.allTransactions
    }

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>> {
        return repository.getTransactionsByType(type)
    }

    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>> {
        return repository.getTransactionsByCategory(category)
    }

    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return repository.getTransactionsBetweenDates(startDate, endDate)
    }

    fun getTotalAmountByType(type: TransactionType, startDate: Date, endDate: Date): LiveData<Double?> {
        return repository.getTotalAmountByType(type, startDate, endDate)
    }

    // Budget operations
    fun getBudgetForMonth(month: Int, year: Int): LiveData<List<Budget>> {
        return repository.getBudgetForMonth(month, year)
    }

    fun getCategoryBudget(category: String, month: Int, year: Int): LiveData<Budget?> {
        return repository.getCategoryBudget(category, month, year)
    }

    fun insertBudget(budget: Budget) = viewModelScope.launch {
        repository.insertBudget(budget)
    }

    fun updateBudget(budget: Budget) = viewModelScope.launch {
        repository.updateBudget(budget)
    }

    fun deleteBudget(budget: Budget) = viewModelScope.launch {
        repository.deleteBudget(budget)
    }

    fun getTotalBudgetForMonth(month: Int, year: Int): LiveData<Double?> {
        return repository.getTotalBudgetForMonth(month, year)
    }
} 