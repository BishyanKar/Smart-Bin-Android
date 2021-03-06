package com.example.smartbin.ui.scanner

import android.Manifest
import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.CountDownTimer
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
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.example.smartbin.Constants
import com.example.smartbin.HomeActivity
import com.example.smartbin.R
import com.example.smartbin.databinding.FragmentScannerBinding
import com.example.smartbin.model.remote.Dispose
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.net.URISyntaxException
import java.util.*
import javax.inject.Inject
import kotlin.math.min


@AndroidEntryPoint
class ScannerFragment : Fragment() {

    private val REQUEST_CHECK_SETTINGS: Int  = 889
    private var _binding: FragmentScannerBinding? = null

    private var locationPermissionGranted: Boolean = false

    private val scannerViewModel by viewModels<ScannerViewModel>()

    @Inject lateinit var sharedPreferences: SharedPreferences
    @Inject lateinit var gson: Gson

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    private lateinit var wasteType: String

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mCodeScanner: CodeScanner? = null
    var scannerView: CodeScannerView? = null

    private lateinit var requestMultiplePermissionForScanner: ActivityResultLauncher<Array<String>>

    private lateinit var authToken: String


    private var mSocket: Socket? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentScannerBinding.inflate(inflater, container, false)
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
                startQRScanner()
            }
        }
        scannerView = binding.scannerView
        mCodeScanner = CodeScanner(requireContext(), scannerView!!)
        mCodeScanner?.decodeCallback = DecodeCallback { result ->
            activity?.runOnUiThread {
                Timber.d("ASC", "run: DECRYPT QR : ${result.text}")
                isValid(result.text)
            }
        }
        mCodeScanner?.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            activity?.runOnUiThread {
                Toast.makeText(context, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
        authToken = sharedPreferences.getString(Constants.KEY_AUTH_TOKEN, null)!!
        try {
            mSocket = IO.socket("${Constants.BASE_URL}user?authorization=$authToken")
        } catch (e: URISyntaxException) {
            e.message
        }
        mSocket?.on(Constants.EVENT_SUCCESS, Emitter.Listener {
            val data = it[0]
            Timber.d("$data")
            if(data as Boolean){
                activity?.runOnUiThread {
                    showDisposeDialog()
                }
            }
        })

        mSocket?.on(Constants.EVENT_CUSTOM_ERROR, Emitter.Listener {
            val data = it[0]
            Timber.d("$data")
            activity?.runOnUiThread {
                showNegativeDialog("Dispose was not successful.\nReason:  $data", DialogInterface.OnCancelListener {
                    activity?.onBackPressed()
                })
            }
        })

        mSocket?.on(Constants.EVENT_TRANSACTION_COMPLETE, Emitter.Listener {
            val data = it[0]
            Timber.d("$data")
            activity?.runOnUiThread {
                showPositiveDialog("Dispose completed successfully.\nYou received $data coins", DialogInterface.OnCancelListener {
                    (activity as HomeActivity).openProfile()
                })
            }
        })
        mSocket?.connect()

    }

    override fun onStart() {
        super.onStart()
        checkingPermissionAndStartScanner()
    }

    override fun onStop() {
        mCodeScanner?.releaseResources()
        super.onStop()
    }

    private fun startDispose(binId: String) {
        if(currentLatitude !=null && currentLongitude != null)
        {
            val dispose = Dispose(binId.trim(), arrayOf(currentLongitude, currentLatitude), wasteType)
            mSocket?.emit(Constants.EVENT_DISPOSE, JSONObject(gson.toJson(dispose)))
        }
    }

    private fun showNegativeDialog(msg: String, onCancelListener: DialogInterface.OnCancelListener) {
        val dialogView: View = layoutInflater.inflate(R.layout.layout_negative_dialog, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
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
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
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

    private fun showDisposeDialog() {
        val dialogView: View = layoutInflater.inflate(R.layout.layout_confirm_dispose, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val tvTimer = dialogView.findViewById<TextView>(R.id.tv_timer)

        val dialog = builder.create()

        btnConfirm.setOnClickListener {
            mSocket?.emit(Constants.EVENT_DISPOSE_DONE)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.cancel()
        }

        if(dialog.window!=null)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
        object : CountDownTimer(5*60*1000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                var millis = millisUntilFinished
                val minute = millis / 60000
                millis %= 60000
                val sec = millis / 1000
                if(sec <= 0)
                    tvTimer.text = "0$minute:0$sec"
                else tvTimer.text = "0$minute:$sec"
            }

            override fun onFinish() {
                tvTimer.text = "Time up!"
            }
        }.start()
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

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                getLocationSettings()
                Toast.makeText(
                    context,
                    "Please turn on your location service",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener {
            getLocationSettings()
            Toast.makeText(
                context,
                "Please allow Location access from the settings",
                Toast.LENGTH_SHORT
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
                } catch (sendEx: SendIntentException) {
                    // Ignore the error.
                }
            }
        })
    }

    private fun createLocationRequest(): LocationRequest? {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
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

    private fun showChooserDialog(binId: String) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val wasteTypes = arrayOf("Wet", "Dry")

        builder.setItems(wasteTypes) { dialogInterface, which ->
            wasteType = wasteTypes[which]
            startDispose(binId)
            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showSelectTypeDialog() {
        val dialogView: View = layoutInflater.inflate(R.layout.layout_select_type_dialog, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val btnMoveToTrash = dialogView.findViewById<Button>(R.id.btn_move_to_trash)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val tvDescription = dialogView.findViewById<TextView>(R.id.tv_description)


        tvDescription.text = ""


        val dialog = builder.create()

        btnMoveToTrash.setOnClickListener {
            Timber.d("Called")
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.cancel()
        }

        if(dialog.window!=null)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
    }

    private fun checkingPermissionAndStartScanner() {
        if (ContextCompat.checkSelfPermission(context?.applicationContext!!, CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context?.applicationContext!!, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(context?.applicationContext!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true
                updateLocationUI()
                startQRScanner()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        requestMultiplePermissionForScanner.launch(arrayOf(CAMERA,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION))
    }


    private fun startQRScanner() {
        mCodeScanner?.startPreview()
    }

    private fun isValid(qrCode: String) {
        showChooserDialog(qrCode)
    }

    override fun onDestroyView() {
        _binding = null
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback!!)

        mSocket?.disconnect()

        mSocket?.off(Constants.EVENT_DISPOSE)
        mSocket?.off(Constants.EVENT_SUCCESS)
        mSocket?.off(Constants.EVENT_TRANSACTION_COMPLETE)
        super.onDestroyView()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_CAMERA = 200
    }
}