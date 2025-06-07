package com.imhungry.jjongseol.data.model.agenda

data class AgendaItem(
    val id: Long?,
    var text: String,
    var isCompleted: Boolean = false,
    var isPlaceholder: Boolean = true
)