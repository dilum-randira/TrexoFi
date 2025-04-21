package com.example.trexofi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.trexofi.R
import com.example.trexofi.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val amountText: TextView = itemView.findViewById(R.id.amountText)
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(transaction: Transaction) {
            titleText.text = transaction.title
            categoryText.text = transaction.category
            dateText.text = dateFormat.format(transaction.date)
            
            val amount = String.format("%.2f", transaction.amount)
            amountText.text = when (transaction.type) {
                TransactionType.INCOME -> "+$$amount"
                TransactionType.EXPENSE -> "-$$amount"
            }
            
            amountText.setTextColor(itemView.context.getColor(
                when (transaction.type) {
                    TransactionType.INCOME -> R.color.income_green
                    TransactionType.EXPENSE -> R.color.expense_red
                }
            ))

            itemView.setOnClickListener { onItemClick(transaction) }
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
} 