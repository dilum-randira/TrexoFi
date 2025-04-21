package com.example.trexofi

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.trexofi.adapter.TransactionAdapter
import com.example.trexofi.data.Transaction
import com.example.trexofi.data.TransactionType
import com.example.trexofi.databinding.ActivityMainBinding
import com.example.trexofi.databinding.DialogAddTransactionBinding
import com.example.trexofi.util.BackupHelper
import com.example.trexofi.util.NotificationHelper
import com.example.trexofi.viewmodel.FinanceViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: FinanceViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var backupHelper: BackupHelper
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val categories = listOf("Food", "Transport", "Bills", "Entertainment", "Shopping", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        notificationHelper = NotificationHelper(this)
        backupHelper = BackupHelper(this)

        setupRecyclerView()
        setupCharts()
        setupFab()
        observeData()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            showEditTransactionDialog(transaction)
        }
        binding.transactionsRecyclerView.adapter = transactionAdapter
    }

    private fun setupCharts() {
        binding.expenseChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            legend.isEnabled = true
            setEntryLabelTextSize(12f)
            setEntryLabelColor(android.R.color.black)
            setHoleRadius(45f)
            setTransparentCircleRadius(50f)
        }
    }

    private fun setupFab() {
        binding.addTransactionFab.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun observeData() {
        viewModel.allTransactions.observe(this) { transactions ->
            transactionAdapter.submitList(transactions)
            updateCharts(transactions)
            checkBudgetAlerts(transactions)
        }
    }

    private fun updateCharts(transactions: List<Transaction>) {
        val expensesByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }

        val pieEntries = expensesByCategory.map { 
            PieEntry(it.value.toFloat(), it.key)
        }

        val dataSet = PieDataSet(pieEntries, "Expenses by Category").apply {
            colors = listOf(
                getColor(R.color.purple_500),
                getColor(R.color.teal_700),
                getColor(R.color.purple_700),
                getColor(R.color.teal_200),
                getColor(R.color.purple_200)
            )
        }

        binding.expenseChart.data = PieData(dataSet)
        binding.expenseChart.invalidate()
    }

    private fun checkBudgetAlerts(transactions: List<Transaction>) {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        viewModel.getTotalBudgetForMonth(currentMonth, currentYear).observe(this) { budget ->
            budget?.let {
                val totalExpenses = transactions
                    .filter { 
                        it.type == TransactionType.EXPENSE &&
                        Calendar.getInstance().apply { time = it.date }.get(Calendar.MONTH) == currentMonth &&
                        Calendar.getInstance().apply { time = it.date }.get(Calendar.YEAR) == currentYear
                    }
                    .sumOf { it.amount }

                val progress = (totalExpenses / budget * 100).toInt()
                binding.budgetProgress.progress = progress
                binding.budgetText.text = "$progress% of budget used"

                if (progress >= 80) {
                    notificationHelper.showBudgetAlert(totalExpenses, budget)
                }
            }
        }
    }

    private fun showAddTransactionDialog(transaction: Transaction? = null) {
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)
        
        // Setup category dropdown
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        (dialogBinding.categoryInput as? AutoCompleteTextView)?.setAdapter(categoryAdapter)

        // Setup date picker
        dialogBinding.dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    dialogBinding.dateInput.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Pre-fill data if editing
        transaction?.let {
            dialogBinding.titleInput.setText(it.title)
            dialogBinding.amountInput.setText(it.amount.toString())
            dialogBinding.categoryInput.setText(it.category)
            dialogBinding.dateInput.setText(dateFormat.format(it.date))
            dialogBinding.noteInput.setText(it.note)
            dialogBinding.typeRadioGroup.check(
                if (it.type == TransactionType.INCOME) R.id.incomeRadio
                else R.id.expenseRadio
            )
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (transaction == null) "Add Transaction" else "Edit Transaction")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val newTransaction = Transaction(
                    id = transaction?.id ?: 0,
                    title = dialogBinding.titleInput.text.toString(),
                    amount = dialogBinding.amountInput.text.toString().toDoubleOrNull() ?: 0.0,
                    category = dialogBinding.categoryInput.text.toString(),
                    type = if (dialogBinding.incomeRadio.isChecked) TransactionType.INCOME
                           else TransactionType.EXPENSE,
                    date = dateFormat.parse(dialogBinding.dateInput.text.toString()) ?: Date(),
                    note = dialogBinding.noteInput.text.toString().takeIf { it.isNotBlank() }
                )

                lifecycleScope.launch {
                    if (transaction == null) {
                        viewModel.insertTransaction(newTransaction)
                    } else {
                        viewModel.updateTransaction(newTransaction)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditTransactionDialog(transaction: Transaction) {
        showAddTransactionDialog(transaction)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_backup -> {
                showBackupDialog()
                true
            }
            R.id.action_restore -> {
                showRestoreDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showBackupDialog() {
        lifecycleScope.launch {
            val transactions = viewModel.allTransactions.value ?: emptyList()
            val budgets = viewModel.getBudgetForMonth(
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.YEAR)
            ).value ?: emptyList()

            if (backupHelper.createBackup(transactions, budgets)) {
                showMessage("Backup created successfully")
            } else {
                showMessage("Failed to create backup")
            }
        }
    }

    private fun showRestoreDialog() {
        AlertDialog.Builder(this)
            .setTitle("Restore Data")
            .setMessage("This will replace all current data with the backup. Continue?")
            .setPositiveButton("Restore") { _, _ ->
                lifecycleScope.launch {
                    backupHelper.restoreBackup()?.let { backupData ->
                        // Clear existing data and restore from backup
                        viewModel.allTransactions.value?.forEach { transaction ->
                            viewModel.deleteTransaction(transaction)
                        }
                        backupData.transactions.forEach { transaction ->
                            viewModel.insertTransaction(transaction)
                        }
                        backupData.budgets.forEach { budget ->
                            viewModel.insertBudget(budget)
                        }
                        showMessage("Data restored successfully")
                    } ?: showMessage("Failed to restore data")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMessage(message: String) {
        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}