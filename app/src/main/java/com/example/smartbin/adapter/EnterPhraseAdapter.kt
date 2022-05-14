package com.example.smartbin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbin.databinding.ItemEnterPhraseBinding

class EnterPhraseAdapter(private val enterPhraseAdapterListener: EnterPhraseAdapterListener): ListAdapter<Int, EnterPhraseAdapter.EnterPhraseVH>(DiffUtilItemCallback) {

    private lateinit var binding: ItemEnterPhraseBinding

    val wordMap = HashMap<Int, String>()
    private lateinit var list: ArrayList<Int>

    private fun setData() {
        submitList(ArrayList())
        submitList(arrayListOf(1,2,3,4,5,6,7,8,9,10,11,12))
        notifyDataSetChanged()
    }

    object DiffUtilItemCallback : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnterPhraseVH {
        binding = ItemEnterPhraseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EnterPhraseVH(binding)
    }

    override fun onBindViewHolder(holder: EnterPhraseVH, position: Int) {
        holder.bind(getItem(position))
    }


    inner class EnterPhraseVH(private val binding: ItemEnterPhraseBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(index: Int) {
            binding.tvIndex.text = "$index"
            if(!wordMap[index].isNullOrEmpty())
                binding.etPhraseInput.setText(wordMap[index])
            binding.etPhraseInput.addTextChangedListener {
                val text =  it.toString().trim()
                val words = text.split(' ')
                if(words.size > 1){
                    var i = 1
                    for(word in words) {
                        wordMap[i++] = word
                    }
        //            enterPhraseAdapterListener.distributeWords(words.size)
                    setData()
                    enterPhraseAdapterListener.textChanged()
                }
                else {
                    wordMap[index] = text
                    enterPhraseAdapterListener.textChanged()
                }
            }
        }
    }
}
interface EnterPhraseAdapterListener {
    fun textChanged()
    fun distributeWords(wordLength: Int)
}