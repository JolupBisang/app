package com.imhungry.sillok.presentation.viewmodel.meeting

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.imhungry.sillok.domain.model.agenda.Agenda
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor() : ViewModel() {
    private val _agendas = MutableStateFlow<List<Agenda>>(emptyList())
    val agendas: StateFlow<List<Agenda>> = _agendas

    private val _isTopSheetExpanded = mutableStateOf(false)
    val isTopSheetExpanded: State<Boolean> get() = _isTopSheetExpanded

    fun setTopSheetExpanded(expanded: Boolean) {
        _isTopSheetExpanded.value = expanded
    }
}