package com.example.smartbin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbin.R
import com.example.smartbin.databinding.LayoutItemHistoryBinding
import com.example.smartbin.model.remote.Transaction
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val context: Context): ListAdapter<Transaction, TransactionAdapter.TransactionVH>(DiffUtilItemCallback) {

    private lateinit var binding: LayoutItemHistoryBinding

    object DiffUtilItemCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.status == newItem.status
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionVH {
        binding = LayoutItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionVH(binding)
    }

    override fun onBindViewHolder(holder: TransactionVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionVH(private val binding: LayoutItemHistoryBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.tvCategory.text = transaction.wasteType
            binding.tvCoin.text = "${transaction.coins}"
            binding.tvRefNo.text = transaction.id

            val originalFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val targetFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val date = transaction.createdAt?.let { originalFormat.parse(it) }
            val formattedDate = date?.let { targetFormat.format(it) }

            binding.tvDate.text = formattedDate

            when(transaction.status) {
                "COMPLETED" -> {
                    if(transaction.coins!! < 0.0){
                        binding.ivIconReceived.setColorFilter(context.getColor(R.color.material_red_800))
                    }
                    else binding.ivIconReceived.setColorFilter(context.getColor(R.color.material_green_800))
                }
                "PENDING" -> {
                    binding.ivIconReceived.setColorFilter(context.getColor(R.color.material_yellow_500))
                }
                else -> binding.ivIconReceived.setColorFilter(context.getColor(R.color.material_red_800))
            }
        }
    }

}