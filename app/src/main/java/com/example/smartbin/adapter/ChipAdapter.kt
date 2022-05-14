package com.example.smartbin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbin.databinding.ItemChipBinding

class ChipAdapter(private val chipAdapterListener: ChipAdapterListener): ListAdapter<String, ChipAdapter.ChipVH>(DiffUtilItemCallback) {

    private lateinit var binding: ItemChipBinding
    private lateinit var list: ArrayList<String>

    object DiffUtilItemCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

    fun setData(list: ArrayList<String>) {
        this.list = list
        submitList(this.list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipVH {
        binding = ItemChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChipVH(binding)
    }

    override fun onBindViewHolder(holder: ChipVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChipVH(private val binding: ItemChipBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.tvPhrase.text = text

            binding.cvChip.setOnClickListener {
                val newList = ArrayList(list)
                newList.remove(text)
                setData(newList)
                chipAdapterListener.onChipSelected(text)
            }
        }
    }
}
interface ChipAdapterListener {
    fun onChipSelected(text: String)
}