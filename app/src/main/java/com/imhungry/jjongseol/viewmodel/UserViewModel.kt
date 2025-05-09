package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import com.imhungry.jjongseol.data.network.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    val userApi: UserApi
) : ViewModel()