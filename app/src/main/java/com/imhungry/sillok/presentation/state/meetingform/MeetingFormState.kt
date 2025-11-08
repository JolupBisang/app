package com.imhungry.sillok.presentation.state.meetingform

import androidx.compose.ui.text.input.TextFieldValue
import com.imhungry.sillok.domain.model.places.PlaceSuggestion

data class MeetingFormState(
    val userName: String = "{이름}",
    val title: String = "",
    val titleTextFieldValue: TextFieldValue = TextFieldValue(""),
    val participants: String = "",
    val participantEmails: List<String> = emptyList(),
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val duration: String = "",
    val location: String = "",
    val locationTextFieldValue: TextFieldValue = TextFieldValue(""),
    val agendas: List<String> = listOf(""), // 기본 아젠다 필드 하나 제공
    val breakInterval: String = "",
    val breakDuration: String = "",

    // 편집 모드 관련 상태
    val isEditMode: Boolean = false,
    val meetingId: Long? = null,

    // 이메일 자동완성 관련 상태
    val emailSuggestions: List<String> = emptyList(),
    val showEmailSuggestions: Boolean = false,
    val isSearching: Boolean = false,

    // 장소 자동완성 관련 상태
    val locationSuggestions: List<PlaceSuggestion> = emptyList(),
    val showLocationSuggestions: Boolean = false,
    val isLocationSearching: Boolean = false,

    // UI 상태
    val isLoading: Boolean = false,
    val error: String? = null,
    val showTimePicker: Boolean = false,
    val showCancelDialog: Boolean = false,

    // 필수 항목 검증 상태
    val validationErrors: Map<String, String> = emptyMap(),
    val showValidationErrors: Boolean = false,
    val isFormValid: Boolean = false
)

// 기존 회의 데이터를 위한 데이터 클래스(편집 모드 초기화에 사용)
data class MeetingData(
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val agendas: List<String>,
    val breakInterval: String,
    val breakDuration: String,
    val participants: List<String>
)