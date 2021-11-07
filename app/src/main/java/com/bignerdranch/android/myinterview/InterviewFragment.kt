package com.bignerdranch.android.myinterview

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.myinterview.databinding.FragmentInterviewBinding
import com.bignerdranch.android.myinterview.model.InterviewViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class InterviewFragment : Fragment() {

    private var binding: FragmentInterviewBinding? = null
    private val sharedViewModel: InterviewViewModel by activityViewModels()
    private var videoCapture = VideoCapture.Builder().build()
    private lateinit var outputDirectory: File
    private var save = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentInterviewBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            binding?.oneFragment = this@InterviewFragment
        }

        outputDirectory = getOutputDirectory()
        startCamera(StartFragment.cameraSelector)

        GlobalScope.launch(Dispatchers.IO) {
            delay(2000L)
            startRecording()
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            stopRecording()
            sharedViewModel.resetVariables()
            GlobalScope.launch(Dispatchers.Main) {
                binding?.progressBar?.isVisible = true
                delay(5000L)
                binding?.progressBar?.isGone = true
                findNavController().navigate(R.id.action_flavorFragment_to_startFragment)
            }
        }
    }

    @DelicateCoroutinesApi
    fun goToNextQuestion() {
        binding?.btnNextQuestion?.isGone = true
        binding?.btnFinish?.isVisible = true
        stopRecording()
        GlobalScope.launch(Dispatchers.IO) {
            delay(2000L)
            startRecording()
        }
        binding?.questionOne?.setText(R.string.question_two)
        binding?.questionOnePrompt?.setText(R.string.question_two_prompt)
        sharedViewModel.updateNumberOfQuestions()

    }

    @DelicateCoroutinesApi
    fun finishInterview() {
        stopRecording()
        GlobalScope.launch(Dispatchers.Main) {
            binding?.progressBar?.isVisible  = true
            delay(5000L)
            binding?.progressBar?.isGone  = true
            findNavController().navigate(R.id.action_interviewFragment_to_summaryFragment)

        }
        sharedViewModel.updateNumberOfQuestions()

    }

    @DelicateCoroutinesApi
    fun cancelInterview() {
        sharedViewModel.resetVariables()
        stopRecording()
        GlobalScope.launch(Dispatchers.Main) {
            binding?.progressBar?.isVisible = true
            delay(5000L)
            binding?.progressBar?.isGone = true
            findNavController().navigate(R.id.action_flavorFragment_to_startFragment)
        }
    }

    private fun startCamera(cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
                }

            videoCapture = VideoCapture.Builder().build()


            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )

            } catch (exc: Exception) {
                Snackbar.make(requireView(), "Use case binding failed", Snackbar.LENGTH_LONG).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun getOutputDirectory(): File {
        val mediaDir = activity?.externalMediaDirs?.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else activity?.filesDir!!
    }

    @SuppressLint("RestrictedApi")
    private fun startRecording() {
        var mySnackbar = Snackbar.make(requireView(), "Recording started", Snackbar.LENGTH_LONG)
        mySnackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.black))
        mySnackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        mySnackbar.show()
        val videoFile = File(
            outputDirectory,
            SimpleDateFormat(
                StartFragment.FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".mp4"
        )
        val outputOptions = VideoCapture.OutputFileOptions.Builder(videoFile).build()

        videoCapture.startRecording(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : VideoCapture.OnVideoSavedCallback {
                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    mySnackbar = Snackbar.make(
                        requireView(),
                        "Video capture failed: $message",
                        Snackbar.LENGTH_LONG
                    )
                    mySnackbar.setBackgroundTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                    mySnackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    mySnackbar.show()
                }

                override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                    if (save) {
                        val savedUri = Uri.fromFile(videoFile)
                        val msg = "Video capture succeeded: $savedUri"
                        mySnackbar = Snackbar.make(
                            requireView(),
                            msg,
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
            })
    }

    @SuppressLint("RestrictedApi")
    private fun stopRecording() {
        videoCapture.stopRecording()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


}