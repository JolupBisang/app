package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import com.imhungry.jjongseol.data.repository.LoginRepository
import com.imhungry.jjongseol.ui.SilRokNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {

    fun getNavigationDestination(): SilRokNavigation =
        if (loginRepository.isLoggedIn()) SilRokNavigation.MeetingWaiting else SilRokNavigation.Login
}

