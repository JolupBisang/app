package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import com.imhungry.jjongseol.data.repository.LoginRepository
import com.imhungry.jjongseol.ui.SilRokNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    loginRepository: LoginRepository
) : ViewModel() {
    val isLoggedIn = loginRepository.isLoggedInFlow
}

