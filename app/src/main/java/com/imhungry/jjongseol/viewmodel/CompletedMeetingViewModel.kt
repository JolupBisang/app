package com.imhungry.jjongseol.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CompletedMeetingViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
}