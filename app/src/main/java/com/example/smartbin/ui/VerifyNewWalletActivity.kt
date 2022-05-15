package com.example.smartbin.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.smartbin.Constants
import com.example.smartbin.HomeActivity
import com.example.smartbin.R
import com.example.smartbin.adapter.ChipAdapter
import com.example.smartbin.adapter.ChipAdapterListener
import com.example.smartbin.adapter.ChipSelectedAdapter
import com.example.smartbin.databinding.ActivityVerifyNewWalletBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VerifyNewWalletActivity : AppCompatActivity(), ChipAdapterListener {

    private lateinit var binding: ActivityVerifyNewWalletBinding

    private lateinit var chipAdapter: ChipAdapter
    private lateinit var chipSelectedAdapter: ChipSelectedAdapter

    private var testString: String? = "1 2 3 4 5 6 7 8 9 10 11 12"

    private var phraseSelected: String = ""

    @Inject lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyNewWalletBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)
        toggleProceedButton(false)

        initListeners()
    }

    override fun onStart() {
        super.onStart()
        testString = sharedPreferences.getString(Constants.KEY_PHRASE, null)
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
        binding.btnProceed.setOnClickListener {
            if(testString == phraseSelected){
                editor = sharedPreferences.edit()
                editor.putString(Constants.KEY_PHRASE, null)
                editor.apply()
                showPositiveDialog("Phrase verified", DialogInterface.OnCancelListener {
                    startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                })
            }
            else {
                showNegativeDialog("Phrase verification failed", DialogInterface.OnCancelListener {

                })
            }
        }
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


    private fun setChipAdapterData() {
        val wordArray = testString?.split(" ")?.shuffled() as ArrayList
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

    private fun toggleProceedButton(flag: Boolean) {
        if(flag) {
            binding.btnProceed.isClickable = true
            binding.btnProceed.isFocusable = true
            binding.btnProceed.isEnabled = true
        }
        else {
            binding.btnProceed.isClickable = false
            binding.btnProceed.isFocusable = false
            binding.btnProceed.isEnabled = false
        }
    }

    override fun onChipSelected(text: String) {
        val currentList = ArrayList(chipSelectedAdapter.currentList)
        currentList.add(text)
        chipSelectedAdapter.setData(currentList)
        if(currentList.size == 12) {
            toggleProceedButton(true)
            for(word in currentList){
                "$phraseSelected$word "
            }
            phraseSelected.trim()
        }
        else toggleProceedButton(false)
    }
}