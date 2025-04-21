package com.example.trexofi.util

import android.content.Context
import com.example.trexofi.data.Transaction
import com.example.trexofi.data.Budget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.util.*

class BackupHelper(private val context: Context) {
    private val gson = Gson()
    private val backupFileName = "finance_backup.json"

    data class BackupData(
        val transactions: List<Transaction>,
        val budgets: List<Budget>,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun createBackup(transactions: List<Transaction>, budgets: List<Budget>): Boolean {
        return try {
            val backupData = BackupData(transactions, budgets)
            val jsonString = gson.toJson(backupData)
            
            context.openFileOutput(backupFileName, Context.MODE_PRIVATE).use { 
                it.write(jsonString.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun restoreBackup(): BackupData? {
        return try {
            val jsonString = context.openFileInput(backupFileName).bufferedReader().use { 
                it.readText() 
            }
            gson.fromJson(jsonString, BackupData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getBackupInfo(): BackupInfo? {
        val file = context.getFileStreamPath(backupFileName)
        return if (file.exists()) {
            BackupInfo(
                file.length(),
                Date(file.lastModified())
            )
        } else null
    }

    data class BackupInfo(
        val size: Long,
        val lastModified: Date
    )
} 