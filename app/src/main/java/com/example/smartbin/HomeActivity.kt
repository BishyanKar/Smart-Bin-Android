package com.example.smartbin

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.smartbin.databinding.ActivityHomeBinding
import com.example.smartbin.ui.ConnectAndVerifyActivity
import com.example.smartbin.ui.VerifyNewWalletActivity
import com.example.smartbin.ui.home.HomeFragment
import com.example.smartbin.ui.home.HomeViewModel
import com.example.smartbin.ui.scanner.ScannerFragment
import com.example.smartbin.viewmodel.HomeActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    @Inject lateinit var sharedPreferences: SharedPreferences

    private val REQUEST_CHECK_SETTINGS: Int  = 889

    private lateinit var navController: NavController

    val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

//    val homeActivityViewModel by viewModels<HomeActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if(sharedPreferences.getString(Constants.KEY_PHRASE, null) != null){
            startActivity(Intent(this, VerifyNewWalletActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode,data)
        if(requestCode == REQUEST_CHECK_SETTINGS) {
            when(resultCode) {
                Activity.RESULT_OK -> {
                    val fragment = supportFragmentManager.currentNavigationFragment
                    if(fragment is HomeFragment)
                        fragment.updateLocationUI()
                    else if(fragment is ScannerFragment)
                        fragment.updateLocationUI()
                }
                else -> {
                    val fragment = supportFragmentManager.currentNavigationFragment
                    if(fragment is HomeFragment) {
                        fragment.showTurnOnLocationDialog()
                    }
                    else if(fragment is ScannerFragment) {
                        fragment.onDestroyView()
                    }
                }
            }
        }
    }

    fun openProfile() {
        navController.navigate(R.id.navigation_notifications)
    }
}