package com.example.trackrunning.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.trackrunning.R
import com.example.trackrunning.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.trackrunning.other.TrackingUtility
import com.example.trackrunning.ui.viewmodels.MainViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import timber.log.Timber


@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    companion object {
        fun newInstance() = RunFragment()
    }

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val BACKGROUND_LOCATION_PERMISSION =
        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    private val PERMISSION_ID = 42

    private val viewModel: MainViewModel by viewModels()

    private val DENIED: String = "DENIED"
    private val EXPLAINED: String = "EXPLAINED"
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val deniedList: List<String> = result.filter {
            !it.value
        }.map {
            it.key
        }
        when {
            deniedList.isNotEmpty() -> {
                val map = deniedList.groupBy { permissions ->
                    if (shouldShowRequestPermissionRationale(permissions)) DENIED else EXPLAINED
                }
                map[DENIED]?.let {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        it.toTypedArray(), PERMISSION_ID
                    )
                }
                map[EXPLAINED]?.let {
                    AppSettingsDialog.Builder(this).build().show()
                }
            }
            else -> {
                Timber.d("All permissions granted")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationPermissionRequest.launch(REQUIRED_PERMISSIONS)
        /*runFragmentBinding.*/fab.setOnClickListener {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                if (!TrackingUtility.hasBackgroundLocationPermission(requireContext())) {
                    val dialog =
                        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                            .setTitle("Need Background Location Permission")
                            .setMessage("To Start Run, MVVM App Tracking Runner requires background location permission")
                            .setPositiveButton("Go To Settings Page") { _, _ ->
                                AppSettingsDialog.Builder(this).build().show()
                            }
                            .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.cancel() }
                            .create()
                    dialog.show()
                    return@setOnClickListener
                }
                findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
            } else {
                findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
            }
        }


    }
}

