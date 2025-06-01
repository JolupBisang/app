package com.imhungry.jjongseol.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.home.MeetingResponse
import com.imhungry.jjongseol.data.network.api.MeetingApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject
import androidx.compose.runtime.*

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val meetingApi: MeetingApi
) : ViewModel() {

    private val _meetings = mutableStateOf<List<MeetingResponse>>(emptyList())
    val meetings: State<List<MeetingResponse>> = _meetings

    fun loadMeetings(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                val response = meetingApi.getMeetings(yearMonth.year, yearMonth.monthValue)

                if (response.isSuccessful) {
                    val meetingsData = response.body()?.data?.meetings ?: emptyList()
                    _meetings.value = meetingsData
                    Log.d("ScheduleViewModel", "Meetings loaded: ${meetingsData.size}건")
                } else {
                    Log.w(
                        "ScheduleViewModel",
                        "API 실패 - code: ${response.code()}, message: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "네트워크 예외 발생: ${e.localizedMessage}", e)
            }
        }
    }
}
