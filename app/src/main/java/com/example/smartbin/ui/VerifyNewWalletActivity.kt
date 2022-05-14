package com.example.smartbin.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.smartbin.R
import com.example.smartbin.adapter.ChipAdapter
import com.example.smartbin.adapter.ChipAdapterListener
import com.example.smartbin.adapter.ChipSelectedAdapter
import com.example.smartbin.databinding.ActivityVerifyNewWalletBinding

class VerifyNewWalletActivity : AppCompatActivity(), ChipAdapterListener {

    private lateinit var binding: ActivityVerifyNewWalletBinding

    private lateinit var chipAdapter: ChipAdapter
    private lateinit var chipSelectedAdapter: ChipSelectedAdapter

    private val testString = "1 2 3 4 5 6 7 8 9 10 11 12"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyNewWalletBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)
        initListeners()
        initRecyclerViews()
    }

    private fun initListeners() {
        binding.actionBar.ivBtnActionBarBack.setOnClickListener {
            onBackPressed()
        }
        binding.actionBar.ivBtnReset.setOnClickListener {
            chipSelectedAdapter.setData(ArrayList())
            setChipAdapterData()
        }
    }

    private fun setChipAdapterData() {
        val wordArray = testString.split(" ") as ArrayList
        chipAdapter.setData(wordArray)
    }

    private fun initRecyclerViews() {
        chipAdapter = ChipAdapter(this)
        binding.rvSelect.adapter = chipAdapter
        setChipAdapterData()

        chipSelectedAdapter = ChipSelectedAdapter()
        binding.rvSelected.adapter = chipSelectedAdapter
        chipSelectedAdapter.setData(ArrayList())

    }

    override fun onChipSelected(text: String) {
        val currentList = ArrayList(chipSelectedAdapter.currentList)
        currentList.add(text)
        chipSelectedAdapter.setData(currentList)
    }
}