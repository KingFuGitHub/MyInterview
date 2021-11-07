package com.bignerdranch.android.myinterview

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.myinterview.databinding.FragmentStartBinding
import com.google.android.material.snackbar.Snackbar


class StartFragment : Fragment() {
    private var binding: FragmentStartBinding? = null

    companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentStartBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.startFragment = this

        askForPermission()
    }

    fun startInterview() {

        if (allPermissionsGranted() && binding?.etPasscode?.editableText.toString() == "1234") {
            findNavController().navigate(R.id.action_startFragment_to_interviewFragment)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        if (binding?.etPasscode?.editableText.toString() != "1234" && allPermissionsGranted()) {
            val mySnackbar = Snackbar.make(
                requireView(),
                "Invalid passcode.",
                Snackbar.LENGTH_SHORT
            )
            mySnackbar.setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
            mySnackbar.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            mySnackbar.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun askForPermission() {
        if (allPermissionsGranted()) {
//            startCamera(cameraSelector)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }


}