package com.example.smartbin.ui.profile

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.smartbin.Constants
import com.example.smartbin.R
import com.example.smartbin.adapter.TransactionAdapter
import com.example.smartbin.databinding.FragmentProfileBinding
import com.example.smartbin.model.remote.User
import com.example.smartbin.ui.ConnectAndVerifyActivity
import com.example.smartbin.ui.VerifyNewWalletActivity
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject
import timber.log.Timber
import kotlin.math.roundToInt

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    @Inject lateinit var sharedPreferences: SharedPreferences
    @Inject lateinit var gson: Gson

    private lateinit var editor: SharedPreferences.Editor

    private lateinit var user: User

    private lateinit var transactionAdapter: TransactionAdapter

    private var totalScans = 0
    private var totalWeights = 0.0

    private val profileViewModel by viewModels<ProfileViewModel>()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editor = sharedPreferences.edit()
        toggleProgressBar(true)
        initRecyclerView()
        initUser()
        initListeners()
    }

    private fun initListeners() {
        binding.btnRedeem.setOnClickListener {
            if(user.wallet == null) {
                showNoWalletDialog()
            }
            else {
                showRedeemDialog()
            }
        }
        binding.llCoin.setOnClickListener {
            if(user.wallet!=null) {
                toggleProgressBarOnly(true)
                getWalletBalance()
            }
            else {
                showNoWalletDialog()
            }
        }
    }

    private fun transfer() {
        profileViewModel.transfer().observe(viewLifecycleOwner, Observer { response ->
            if(response.body != null){
                val transferResponse = response.body

                if(!transferResponse.error){
                    showPositiveDialog("Coins transferred successfully", DialogInterface.OnCancelListener {
                        //do nothing
                    }, "")
                }
                else {
                    Timber.d(transferResponse.message)
                    showNegativeDialog("Transfer failed", DialogInterface.OnCancelListener {
                        //do nothing
                    })
                    Toast.makeText(context, "${transferResponse.message}", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Timber.e(response.errorMessage)
                showNegativeDialog("Transfer failed", DialogInterface.OnCancelListener {
                    //do nothing
                })
                Toast.makeText(context, "${response.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            toggleProgressBarOnly(false)
        })
    }

    private fun showRedeemDialog() {
        val dialogView: View = layoutInflater.inflate(R.layout.layout_redeem, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

        val dialog = builder.create()

        btnConfirm.setOnClickListener {
            toggleProgressBarOnly(true)
            dialog.dismiss()
            transfer()
        }

        btnCancel.setOnClickListener {
            dialog.cancel()
        }

        if(dialog.window!=null)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
    }

    private fun showPositiveDialog(msg: String, onCancelListener: DialogInterface.OnCancelListener, phrase: String) {
        val dialogView: View = layoutInflater.inflate(R.layout.layout_positive_dialog, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val btnContinue = dialogView.findViewById<Button>(R.id.btn_continue)
        val tvMsg = dialogView.findViewById<TextView>(R.id.tv_msg)
        val layoutPhrase = dialogView.findViewById<LinearLayout>(R.id.layout_phrase)

        if(phrase != "") {
            layoutPhrase.visibility = View.VISIBLE
        }
        else layoutPhrase.visibility = View.GONE

        val tvPhrase = dialogView.findViewById<TextView>(R.id.tv_phrase)
        val ibCopy = dialogView.findViewById<ImageButton>(R.id.ib_copy)

        tvMsg.text = msg
        tvPhrase.text = phrase

        ibCopy.setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("phrase", phrase)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

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
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
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

    private fun createNewWallet() {
        profileViewModel.connectToNewWallet().observe(viewLifecycleOwner, Observer { response ->
            if(response.body!=null) {
                val profileResponse = response.body

                if(!profileResponse.err){
                    //success
                    editor.putString(Constants.KEY_PHRASE, profileResponse.phrase)
                    editor.apply()
                    showPositiveDialog("Wallet created successfully, copy and save your phrase securely. Never lose it.", DialogInterface.OnCancelListener {
                        startActivity(Intent(context, VerifyNewWalletActivity::class.java))
                    }, profileResponse.phrase!!)
                }
                else {
                    Timber.e(profileResponse.message)
                    showNegativeDialog("Wallet creation failed", DialogInterface.OnCancelListener {
                        //do nothing
                    })
                    Toast.makeText(context, "${profileResponse.message}", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Timber.e(response.errorMessage)
                showNegativeDialog("Wallet creation failed", DialogInterface.OnCancelListener {
                    //do nothing
                })
                Toast.makeText(context, "${response.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            toggleProgressBar(false)
        })
    }

    private fun getWalletBalance() {
        profileViewModel.getWalletBalance().observe(viewLifecycleOwner, Observer { response ->
            if(response.body != null) {
                val balanceResponse = response.body

                if(!balanceResponse.error){
                    showWalletDialog("${balanceResponse.coins}", balanceResponse.wallet!!.balance)
                }
                else {
                    Timber.d(balanceResponse.message)
                    Toast.makeText(context, "${balanceResponse.message}", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Timber.d(response.errorMessage)
                Toast.makeText(context, "${response.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            toggleProgressBarOnly(false)
        })
    }

    private fun showWalletDialog(ourCoins: String, hireCoins: String) {
        val dialogView: View = layoutInflater.inflate(R.layout.layout_wallet, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val btnDisconnect = dialogView.findViewById<Button>(R.id.btn_disconnect)
        val tvCoin = dialogView.findViewById<TextView>(R.id.tv_coin)
        val tvHireCoin = dialogView.findViewById<TextView>(R.id.tv_hire_coin)

        val dialog = builder.create()

        tvCoin.text = ourCoins
        tvHireCoin.text = hireCoins

        btnDisconnect.setOnClickListener {
            dialog.dismiss()
            toggleProgressBarOnly(true)
            disconnectWallet()
        }

        if(dialog.window!=null)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
    }

    private fun disconnectWallet() {
        profileViewModel.disconnectWallet().observe(viewLifecycleOwner, Observer { response ->
            if(response.body!=null) {
                val profileResponse = response.body

                if(!profileResponse.err){
                    //success
                    showPositiveDialog("Wallet has been disconnected. Redeem service will be impacted", DialogInterface.OnCancelListener {
                        toggleProgressBar(true)
                        initUser()
                    }, "")
                }
                else {
                    Timber.e(profileResponse.message)
                    showNegativeDialog("Attempt to disconnect wallet was unsuccessful, please try again after a while", DialogInterface.OnCancelListener {
                        //do nothing
                    })
                    Toast.makeText(context, "${profileResponse.message}", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Timber.e(response.errorMessage)
                showNegativeDialog("Attempt to disconnect wallet was unsuccessful, please try again after a while", DialogInterface.OnCancelListener {
                    //do nothing
                })
                Toast.makeText(context, "${response.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            toggleProgressBarOnly(false)
        })
    }

    private fun showNoWalletDialog() {
        val dialogView: View = layoutInflater.inflate(R.layout.layout_connect, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val btnCreate = dialogView.findViewById<Button>(R.id.btn_create)
        val btnConnect = dialogView.findViewById<Button>(R.id.btn_connect_existing)

        val dialog = builder.create()

        btnCreate.setOnClickListener {
            toggleProgressBar(true)
            dialog.dismiss()
            createNewWallet()
        }

        btnConnect.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(context, ConnectAndVerifyActivity::class.java))
        }

        if(dialog.window!=null)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
    }

    private fun toggleProgressBar(flag: Boolean) {
        if(flag) {
            binding.progressCircular.visibility = View.VISIBLE
            binding.nestedScrollView.visibility = View.GONE
        }
        else {
            binding.progressCircular.visibility = View.GONE
            binding.nestedScrollView.visibility = View.VISIBLE
        }
    }

    private fun toggleProgressBarOnly(flag: Boolean) {
        if(flag) {
            binding.progressCircular.visibility = View.VISIBLE
        }
        else {
            binding.progressCircular.visibility = View.GONE
        }
    }

    private fun initRecyclerView() {
        transactionAdapter = TransactionAdapter(requireContext())
        binding.rvHistory.adapter = transactionAdapter
        binding.rvHistory.layoutManager = LinearLayoutManager(context)
    }

    private fun getAllTransactions() {
        profileViewModel.getAllTransactions().observe(viewLifecycleOwner, Observer { response ->
            if(response.body!=null) {
                val transactionResponse = response.body
                val transactions = transactionResponse.transactions
                totalScans = transactions?.size!!
                binding.tvScanCount.text = "$totalScans"
                if(transactions.isEmpty()){
                    binding.tvHistoryPlaceholder.text = context?.getString(R.string.no_records)
                    binding.tvHistoryPlaceholder.visibility = View.VISIBLE
                }
                else {
                    binding.tvHistoryPlaceholder.visibility = View.GONE
                    for(transaction in transactions!!){
                        totalWeights += transaction.weight!!
                    }
                }

                binding.tvWeightMeter.text = "$totalWeights kg"

                transactionAdapter.submitList(transactions)
            }
            else {
                Timber.e(response.errorMessage)
                binding.tvHistoryPlaceholder.text = "Some error occurred"
                binding.tvHistoryPlaceholder.visibility = View.VISIBLE
                Toast.makeText(context, "${response.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            toggleProgressBar(false)
        })
    }

    private fun initUI() {
        binding.tvName.text = user.name
        binding.tvEmail.text = user.email
        binding.tvCoin.text = ((user.ourCoins?.times(10.0))?.roundToInt()?.div(10.0)).toString()
        try {
            Glide
                .with(requireActivity())
                .load(user.avatar)
                .centerCrop()
                .placeholder(R.drawable.round_account_circle_20)
                .into(binding.ivProfileImage);
        }catch (e: Exception) {
            Timber.e(e.message)
        }
        getAllTransactions()
    }

    private fun initUser() {
        profileViewModel.getProfileInfo().observe(viewLifecycleOwner, Observer { response ->
            if(response.body!=null){
                val profileRes = response.body

                if(!profileRes.err) {
                    user = profileRes.user!!
                    editor.putString(Constants.KEY_USER, gson.toJson(user))
                    editor.apply()
                    initUI()
                }
                else {
                    Timber.e(profileRes.message)
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Timber.e(response.errorMessage)
                Toast.makeText(context, "${response.errorMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}