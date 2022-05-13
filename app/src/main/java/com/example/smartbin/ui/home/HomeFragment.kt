package com.example.smartbin.ui.home

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
import com.example.smartbin.R
import com.example.smartbin.adapter.BinAdapter
import com.example.smartbin.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val homeViewModel by viewModels<HomeViewModel>()

    private lateinit var binAdapter: BinAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleProgressBar(true)
        initRecyclerView()
        getAllBins()
    }

    private fun toggleProgressBar(flag: Boolean) {
        if(flag) {
            binding.progressCircular.visibility = View.VISIBLE
            binding.llHome.visibility = View.GONE
        }
        else {
            binding.progressCircular.visibility = View.GONE
            binding.llHome.visibility = View.VISIBLE
        }
    }

    private fun initRecyclerView() {
        binAdapter = BinAdapter()
        binding.rvAllBins.layoutManager = LinearLayoutManager(context)
        binding.rvAllBins.adapter = binAdapter
    }


    private fun getAllBins() {
        homeViewModel.getAllBins().observe(viewLifecycleOwner, Observer { response ->
            if(response.body!=null) {
                val bins = response.body.bins

                if(bins == null){
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                    binding.tvHistoryPlaceholder.text = "Some error occurred"
                    binding.tvHistoryPlaceholder.visibility = View.VISIBLE
                }
                else if(bins.isEmpty()){
                    binding.tvHistoryPlaceholder.text = context?.getString(R.string.no_records)
                    binding.tvHistoryPlaceholder.visibility = View.VISIBLE
                }
                else {
                    binding.tvHistoryPlaceholder.visibility = View.GONE
                    binAdapter.submitList(bins)
                }
            }
            else {
                Timber.e(response.errorMessage)
                Toast.makeText(context, "${response.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            toggleProgressBar(false)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}