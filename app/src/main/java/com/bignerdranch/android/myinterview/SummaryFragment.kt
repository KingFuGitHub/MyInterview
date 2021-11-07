package com.bignerdranch.android.myinterview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.myinterview.databinding.FragmentSummaryBinding
import com.bignerdranch.android.myinterview.model.InterviewViewModel

class SummaryFragment : Fragment() {

    private var binding: FragmentSummaryBinding? = null
    private val sharedViewModel: InterviewViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentSummaryBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            binding?.summaryFragment = this@SummaryFragment
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            sharedViewModel.resetVariables()
            findNavController().navigate(R.id.action_summaryFragment_to_startFragment)
        }

    }

    fun sendOrder() {

        val orderSummary = getString(
            R.string.order_details,
            sharedViewModel.date.value.toString(),
            resources.getString(R.string.question_one_prompt),
            resources.getString(R.string.your_answer_one),
            resources.getString(R.string.question_two_prompt),
            resources.getString(R.string.your_answer_two),
            sharedViewModel.numberOfQuestions.value.toString()
        )

        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.interview_summary))
            .putExtra(Intent.EXTRA_TEXT, orderSummary)


        if (activity?.packageManager?.resolveActivity(intent, 0) != null) {
            startActivity(intent)
        }
    }

    fun cancelOrder() {
        sharedViewModel.resetVariables()
        findNavController().navigate(R.id.action_summaryFragment_to_startFragment)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}