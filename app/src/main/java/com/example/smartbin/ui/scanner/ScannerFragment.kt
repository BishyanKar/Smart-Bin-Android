package com.example.smartbin.ui.scanner

import android.Manifest
import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.example.smartbin.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class ScannerFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private val scannerViewModel by viewModels<ScannerViewModel>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mCodeScanner: CodeScanner? = null
    var scannerView: CodeScannerView? = null

    private lateinit var requestMultiplePermissionForScanner: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestMultiplePermissionForScanner = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var flag = true
            permissions.entries.forEach { permission ->
                if(!permission.value)
                    flag = false
            }
            if(flag){
                startQRScanner()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkingPermissionIsEnabled()
    }

    private fun checkingPermissionIsEnabled() {
        if (ContextCompat.checkSelfPermission(context?.applicationContext!!, CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context?.applicationContext!!, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(context?.applicationContext!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startQRScanner()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        requestMultiplePermissionForScanner.launch(arrayOf(CAMERA,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION))
    }


    private fun startQRScanner() {
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
        mCodeScanner?.startPreview()
    }

    private fun isValid(qrCode: String) {
        //todo check validity later
        Toast.makeText(context, qrCode, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_CAMERA = 200
    }
}