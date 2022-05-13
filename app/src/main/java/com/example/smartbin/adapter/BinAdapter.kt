package com.example.smartbin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbin.databinding.ItemBinBinding
import com.example.smartbin.model.remote.Bin

class BinAdapter: ListAdapter<Bin, BinAdapter.BinVH>(DiffUtilItemCallback) {

    private lateinit var binding: ItemBinBinding

    object DiffUtilItemCallback : DiffUtil.ItemCallback<Bin>() {
        override fun areItemsTheSame(oldItem: Bin, newItem: Bin): Boolean {
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: Bin, newItem: Bin): Boolean {
            return oldItem.updatedAt == newItem.updatedAt
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BinVH {
        binding = ItemBinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BinVH(binding)
    }

    override fun onBindViewHolder(holder: BinVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BinVH(private val binding: ItemBinBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(bin: Bin) {
            binding.distance.text = "${bin.relativeDistance} km"
            binding.tvLocation.text = "${bin.name}, ${bin.location?.address}, ${bin.location?.city}, ${bin.location?.state}, ${bin.location?.country}"
        }
    }
}