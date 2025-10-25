package com.imhungry.sillok.presentation.util

import com.imhungry.sillok.R

object ProfileUtils {
    private val profileDrawables = listOf(
        R.drawable.profile2,
        R.drawable.profile3,
        R.drawable.profile4,
        R.drawable.profile5,
        R.drawable.profile6,
        R.drawable.profile7,
        R.drawable.profile8,
        R.drawable.profile9,
        R.drawable.profile10,
        R.drawable.profile11,
    )

    fun getProfileDrawableForUser(userId: Long?): Int? {
        if (userId == null) return null
        val idx = (userId % profileDrawables.size).toInt()
        return profileDrawables[idx]
    }
}


