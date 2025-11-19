package com.imhungry.sillok.presentation.util

import com.imhungry.sillok.R

object ProfileUtils {
    private val images = listOf(
        "https://velog.velcdn.com/images/eungyeong12/post/f63ba061-dc4a-46ec-afd7-b979ec743696/image.png",
        "https://velog.velcdn.com/images/eungyeong12/post/6e07cbc8-b032-456a-88e7-1a857cdd0c35/image.png",
        "https://velog.velcdn.com/images/eungyeong12/post/3f25f519-9aa3-4776-8837-4cb826677f6b/image.png",
        "https://velog.velcdn.com/images/eungyeong12/post/63a672a6-8cbf-46c3-8460-de24e9d53a43/image.png",
        "https://velog.velcdn.com/images/eungyeong12/post/63866bdd-bb9d-45b3-9c15-72aad04fc7b0/image.png",
        "https://velog.velcdn.com/images/eungyeong12/post/d5b3e3eb-4a60-4629-8bd9-2bf623c3e7f5/image.png",
        "https://velog.velcdn.com/images/eungyeong12/post/b54de079-fe96-45ae-9faf-e510fcfee495/image.png",
        "https://velog.velcdn.com/images/eungyeong12/post/178bb761-878b-493d-bbc0-5fd19c810ab4/image.png",
        "https://velog.velcdn.com/images/eungyeong12/post/8a857e53-98d5-430b-96c6-3a461b908eea/image.png",
        "https://velog.velcdn.com/images/eungyeong12/post/12ca91c6-9f1a-47f7-ae3f-7d2d87dcccce/image.png",
        "https://velog.velcdn.com/images/eungyeong12/post/8fbfd4fa-dcea-4297-8f2b-e1044136e81f/image.png",
        "https://velog.velcdn.com/images/eungyeong12/post/77ddd37e-6c94-4033-9415-553cffbae12b/image.png"
    )

    fun getProfileImagesForUser(userId: Long): String {
        val idx = (userId % images.size).toInt()
        return images[idx]
    }
}


