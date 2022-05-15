package com.example.smartbin.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartbin.HomeActivity
import com.example.smartbin.R
import com.example.smartbin.adapter.BinAdapter
import com.example.smartbin.adapter.BinAdapterListener
import com.example.smartbin.databinding.FragmentHomeBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.zxing.ResultMetadataType
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.IOException
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment(), BinAdapterListener {

    private var _binding: FragmentHomeBinding? = null
    private val homeViewModel by viewModels<HomeViewModel>()

    private var locationPermissionGranted: Boolean = false

    private lateinit var binAdapter: BinAdapter

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    private val REQUEST_CHECK_SETTINGS: Int  = 889

    private lateinit var requestMultiplePermissionForScanner: ActivityResultLauncher<Array<String>>

//    private val homeActivityViewModel = (activity as HomeActivity).homeActivityViewModel

    var currentLatitude: Double? = null
    var currentLongitude: Double? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                for (location in locationResult.locations) {
                    // Update UI with location data
                    // ...
                    Timber.d("$location")
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude

                    Timber.d("${location.altitude}")

                    if(!homeViewModel.binsLoaded)
                        getAllBins()
                }
            }
        }

        requestMultiplePermissionForScanner = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var flag = true
            permissions.entries.forEach { permission ->
                if(!permission.value)
                    flag = false
            }
            if(flag){
                updateLocationUI()
            }
        }


        toggleProgressBar(true)
        initRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        homeViewModel.binsLoaded = false
        checkingPermissionGetBins()
    }

    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback!!)
    }

    private fun checkingPermissionGetBins() {
        if (ContextCompat.checkSelfPermission(context?.applicationContext!!,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context?.applicationContext!!, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(context?.applicationContext!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            updateLocationUI()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        requestMultiplePermissionForScanner.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION))
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
        binAdapter = BinAdapter(this)
        binAdapter.setLocationData(0.0,0.0)
        binding.rvAllBins.layoutManager = LinearLayoutManager(context)
        binding.rvAllBins.adapter = binAdapter
    }


    private fun getAllBins() {
        if(currentLatitude == null || currentLongitude == null){
            Toast.makeText(context, "Please turn on your location", Toast.LENGTH_SHORT).show()
            activity?.finish()
            return
        }
        homeViewModel.getAllBins(currentLatitude!!, currentLongitude!!).observe(viewLifecycleOwner, Observer { response ->
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
                    homeViewModel.binsLoaded = true
                }
                else {
                    binding.tvHistoryPlaceholder.visibility = View.GONE
                    binAdapter.submitList(bins)
                    homeViewModel.binsLoaded = true
                }
            }
            else {
                Timber.e(response.errorMessage)
                Toast.makeText(context, "${response.errorMessage}", Toast.LENGTH_SHORT).show()
            }
            toggleProgressBar(false)
        })
    }

    @SuppressLint("MissingPermission")
     fun updateLocationUI() {
        fusedLocationProviderClient!!.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    //Toast.makeText(getApplicationContext(),addresses+"",Toast.LENGTH_SHORT).show();
                    val lat1 = addresses[0].latitude
                    val lang1 = addresses[0].longitude


                    currentLatitude = lat1
                    currentLongitude = lang1
                    //double dist = calculateDistance(lat1,lang1,lat1,lang1);

                    binding.tvLocation.text = addresses[0].getAddressLine(0)

                    if(!homeViewModel.binsLoaded)
                        getAllBins()

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                getLocationSettings()
//                Toast.makeText(
//                    context,
//                    "Please turn on your location service",
//                    Toast.LENGTH_SHORT
//                ).show()
            }
        }.addOnFailureListener {
            getLocationSettings()
            Toast.makeText(
                context,
                "Please allow Location access from your phone settings",
                Toast.LENGTH_LONG
            ).show()
        }
        getLocationSettings()
    }

     private fun getLocationSettings() {
        val locationRequest: LocationRequest = createLocationRequest()!!
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val result = LocationSettingsRequest.Builder()
        val client = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener(requireActivity(), OnSuccessListener<LocationSettingsResponse?> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            Timber.d(
                "onSuccess: CALLING LOC UPDATE FROM SUCCESS...."
            )
            startLocationRequests(locationRequest)
        })
        task.addOnFailureListener(requireActivity(), OnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        requireActivity(),
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    Timber.e(sendEx)
                }
            }
        })
    }

    fun showTurnOnLocationDialog() {
        val dialogView: View = layoutInflater.inflate(R.layout.layout_turn_on_location_dialog, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        val dialog = builder.create()

        val btnGiveAccess = dialogView.findViewById<Button>(R.id.btn_give_access)
        val btnExit = dialogView.findViewById<Button>(R.id.btn_exit)

        btnGiveAccess.setOnClickListener {
            dialog.cancel()
            updateLocationUI()
        }

        btnExit.setOnClickListener {
            activity?.finish()
        }

        dialog.setCanceledOnTouchOutside(false)

        if(dialog.window!=null)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
    }

    private fun createLocationRequest(): LocationRequest? {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    @SuppressLint("MissingPermission")
    private fun startLocationRequests(locationRequest: LocationRequest) {
        if (locationPermissionGranted) {
            Timber.d(
                "onSuccess: Requesting location update"
            )
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(lat: Double, lng: Double) {
        val gmmIntentUri = Uri.parse("geo:$lng,$lat?q=$lng,$lat")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//        mapIntent.setPackage("com.google.android.apps.maps")
        mapIntent.resolveActivity(activity?.packageManager!!)?.let {
            startActivity(mapIntent)
        }
    }
}