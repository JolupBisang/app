package com.imhungry.sillok.presentation.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.local.NotificationHistoryStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationHistoryViewModel @Inject constructor(
    private val notificationHistoryStore: NotificationHistoryStore
) : ViewModel() {

    val notificationHistory = notificationHistoryStore.notificationHistory

    fun clearHistory() {
        viewModelScope.launch {
            notificationHistoryStore.clearHistory()
        }
    }
}