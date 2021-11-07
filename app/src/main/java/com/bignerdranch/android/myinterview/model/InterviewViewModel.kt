package com.bignerdranch.android.myinterview.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*


class InterviewViewModel(application: Application) : AndroidViewModel(application) {

    private val _date = MutableLiveData<String>()
    val date: LiveData<String> = _date

    private val _numberOfQuestions = MutableLiveData<Int>()
    val numberOfQuestions: LiveData<Int> = _numberOfQuestions

    private val dateOptions = currentDate()

    fun updateNumberOfQuestions() {
        _numberOfQuestions.value = _numberOfQuestions.value?.plus(1)
    }

    private fun currentDate(): String {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("E M/d/y", Locale.getDefault()).format(calendar.time)
        calendar.add(Calendar.DATE, 1)
        return formatter
    }

    fun resetVariables() {
        _date.value = dateOptions
        _numberOfQuestions.value = 0
    }

    init {
        resetVariables()
    }

}