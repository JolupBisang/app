package com.imhungry.sillok.data.remote.user

import com.google.gson.Gson
import com.imhungry.sillok.data.model.user.UserInfoResDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class UserApiMockWebServerTest {

    private lateinit var server: MockWebServer
    private lateinit var api: UserApi
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getUserInfo_contract() = runTest {
        val body = gson.toJson(UserInfoResDto(id = 1, email = "a@a.com", nickname = "n"))
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val resp = api.getUserInfo("a@a.com")

        val req = server.takeRequest()
        assertEquals("/api/users/a@a.com", req.path)
        assertEquals("GET", req.method)
        assertTrue(resp.isSuccessful)
        assertEquals("n", resp.body()!!.nickname)
    }

    @Test
    fun getMyProfile_contract() = runTest {
        val body = gson.toJson(UserInfoResDto(id = 2, email = "b@b.com", nickname = "m"))
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val resp = api.getMyProfile()

        val req = server.takeRequest()
        assertEquals("/api/users/my-profile", req.path)
        assertEquals("GET", req.method)
        assertTrue(resp.isSuccessful)
        assertEquals("m", resp.body()!!.nickname)
    }
}


