package com.example.smartbin.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.smartbin.Constants
import com.example.smartbin.HomeActivity
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
            //Toast.makeText(this, profileViewModel.currentPhrase, Toast.LENGTH_SHORT).show()
            toggleProgressBar(true)
            connectExistingWallet(profileViewModel.currentPhrase.trim())
        }
        binding.actionBar.ivBtnActionBarBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun toggleProgressBar(flag: Boolean) {
        if(flag) {
            binding.progressCircular.visibility = View.VISIBLE
        }
        else {
            binding.progressCircular.visibility = View.GONE
        }
    }

    private fun showPositiveDialog(msg: String, onCancelListener: DialogInterface.OnCancelListener) {
        val dialogView: View = layoutInflater.inflate(R.layout.layout_positive_dialog, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val btnContinue = dialogView.findViewById<Button>(R.id.btn_continue)
        val tvMsg = dialogView.findViewById<TextView>(R.id.tv_msg)

        tvMsg.text = msg

        val dialog = builder.create()

        btnContinue.setOnClickListener {
            dialog.cancel()
        }

        dialog.setOnCancelListener(onCancelListener)

        if(dialog.window!=null)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
    }

    private fun showNegativeDialog(msg: String, onCancelListener: DialogInterface.OnCancelListener) {
        val dialogView: View = layoutInflater.inflate(R.layout.layout_negative_dialog, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val btnContinue = dialogView.findViewById<Button>(R.id.btn_continue)
        val tvMsg = dialogView.findViewById<TextView>(R.id.tv_msg)

        tvMsg.text = msg

        val dialog = builder.create()

        btnContinue.setOnClickListener {
            dialog.cancel()
        }

        dialog.setOnCancelListener(onCancelListener)

        if(dialog.window!=null)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
    }

    private fun connectExistingWallet(phrase: String) {
        profileViewModel.connectToExistingWallet(phrase).observe(this, Observer { response ->
            if(response.body!=null) {
                val profileResponse = response.body

                if(!profileResponse.err){
                    //success
                    showPositiveDialog("Wallet connected successfully", DialogInterface.OnCancelListener {
                        startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    })
                }
                else {
                    Timber.e(profileResponse.message)
                    showNegativeDialog("Failed to connect your wallet", DialogInterface.OnCancelListener {
                        finish()
                    })
                    Toast.makeText(this, "${profileResponse.message}", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Timber.e(response.errorMessage)
                showNegativeDialog("Failed to connect your wallet", DialogInterface.OnCancelListener {
                    finish()
                })
                Toast.makeText(this, "${response.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            toggleProgressBar(false)
        })
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