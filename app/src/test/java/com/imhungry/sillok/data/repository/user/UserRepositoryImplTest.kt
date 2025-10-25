package com.imhungry.sillok.data.repository.user

import com.imhungry.sillok.data.mapper.user.UserMapper
import com.imhungry.sillok.data.model.user.UserInfoResDto
import com.imhungry.sillok.data.remote.user.UserApi
import com.imhungry.sillok.data.util.ApiResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private val api: UserApi = mockk()
    private val mapper = UserMapper()
    private val repository = UserRepositoryImpl(api, mapper)

    @Test
    fun getUserInfo_success_mapsDomain() = runTest {
        val dto = UserInfoResDto(id = 1L, email = "a@a.com", nickname = "nick")
        coEvery { api.getUserInfo("a@a.com") } returns Response.success(dto)

        val result = repository.getUserInfo("a@a.com")

        assertTrue(result is ApiResult.Success)
        val user = (result as ApiResult.Success).data
        assertEquals("nick", user.nickname)
    }
}


