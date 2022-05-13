package com.example.smartbin.ui.profile

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.smartbin.Constants
import com.example.smartbin.R
import com.example.smartbin.adapter.TransactionAdapter
import com.example.smartbin.databinding.FragmentProfileBinding
import com.example.smartbin.model.remote.User
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject
import timber.log.Timber

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
        binding.tvCoin.text = "${user.ourCoins}"
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