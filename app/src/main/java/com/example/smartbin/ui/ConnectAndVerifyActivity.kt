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
import com.example.smartbin.databinding.ActivityConnectAndVerifyBinding
import com.example.smartbin.model.remote.User
import com.example.smartbin.ui.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ConnectAndVerifyActivity : AppCompatActivity() {

    private val profileViewModel by viewModels<ProfileViewModel>()

    private lateinit var user: User

    private lateinit var _binding: ActivityConnectAndVerifyBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityConnectAndVerifyBinding.inflate(layoutInflater)

        setContentView(binding.root)
        toggleProgressBar(true)
        initUser()
    }

    private fun initUI() {

    }

    private fun toggleProgressBar(flag: Boolean) {
        if(flag) {
            binding.progressCircular.visibility = View.VISIBLE
            binding.clContainer.visibility = View.GONE
        }
        else {
            binding.progressCircular.visibility = View.GONE
            binding.clContainer.visibility = View.VISIBLE
        }
    }

    private fun initUser() {
        profileViewModel.getProfileInfo().observe(this, Observer { response ->
            if(response.body!=null){
                val profileRes = response.body

                if(!profileRes.err) {
                    user = profileRes.user!!
                    initUI()
                }
                else {
                    Timber.e(profileRes.message)
                    Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Timber.e(response.errorMessage)
                Toast.makeText(this, "${response.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            toggleProgressBar(false)
        })
    }
}