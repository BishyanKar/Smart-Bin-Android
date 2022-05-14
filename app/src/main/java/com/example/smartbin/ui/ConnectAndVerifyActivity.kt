package com.example.smartbin.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.smartbin.Constants
import com.example.smartbin.R
import com.example.smartbin.adapter.EnterPhraseAdapter
import com.example.smartbin.adapter.EnterPhraseAdapterListener
import com.example.smartbin.databinding.ActivityConnectAndVerifyBinding
import com.example.smartbin.model.remote.User
import com.example.smartbin.ui.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ConnectAndVerifyActivity : AppCompatActivity(), EnterPhraseAdapterListener {

    private val profileViewModel by viewModels<ProfileViewModel>()

    private lateinit var user: User

    private lateinit var enterPhraseAdapter: EnterPhraseAdapter

    private lateinit var _binding: ActivityConnectAndVerifyBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityConnectAndVerifyBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)

        toggleConfirmButton(false)

        initUI()
        initListeners()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        enterPhraseAdapter = EnterPhraseAdapter(this)
        binding.rvEnterPhrase.adapter = enterPhraseAdapter
        enterPhraseAdapter.submitList(arrayListOf(1,2,3,4,5,6,7,8,9,10,11,12))
    }
    private fun initListeners() {
        binding.btnConfirm.setOnClickListener {
            // call /connect route
            profileViewModel.currentPhrase = ""
            val keyList = enterPhraseAdapter.wordMap.keys
            for(key in keyList){
                profileViewModel.currentPhrase += enterPhraseAdapter.wordMap[key]?.trim() + " "
            }
            Timber.d(profileViewModel.currentPhrase)
            Toast.makeText(this, profileViewModel.currentPhrase, Toast.LENGTH_SHORT).show()
        }
        binding.actionBar.ivBtnActionBarBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun toggleConfirmButton(flag: Boolean) {
        if(flag) {
            binding.btnConfirm.isClickable = true
            binding.btnConfirm.isFocusable = true
            binding.btnConfirm.isEnabled = true
        }
        else {
            binding.btnConfirm.isClickable = false
            binding.btnConfirm.isFocusable = false
            binding.btnConfirm.isEnabled = false
        }
    }

    private fun initUI() {
        binding.actionBar.ivBtnReset.visibility = View.INVISIBLE
        binding.actionBar.tvActionBarTitle.text = getString(R.string.connect_wallet)
    }

    override fun textChanged() {
        //do nothing
        var count = 0
        for(word in enterPhraseAdapter.wordMap.values) {
            if(word != ""){
                count++
            }
        }
        if(count == 12){
            toggleConfirmButton(true)
        }
        else {
            toggleConfirmButton(false)
        }

    }

    override fun distributeWords(wordLength: Int) {
//        if(wordLength == 12){
//            toggleConfirmButton(true)
//        }
//        else {
//            toggleConfirmButton(false)
//        }
        enterPhraseAdapter.submitList(ArrayList())
        enterPhraseAdapter.submitList(arrayListOf(1,2,3,4,5,6,7,8,9,10,11,12))
    }
}