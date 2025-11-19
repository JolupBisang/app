package com.imhungry.sillok.data.remote.folder

import com.imhungry.sillok.data.model.folder.MeetingFolderCreationReqDto
import com.imhungry.sillok.data.model.folder.MeetingFolderCreationResDto
import com.imhungry.sillok.data.model.folder.MeetingFolderDeletionReqDto
import com.imhungry.sillok.data.model.folder.MeetingFolderDeletionResDto
import com.imhungry.sillok.data.model.folder.MeetingFolderDetailResDto
import com.imhungry.sillok.data.model.folder.MeetingFolderListResDto
import retrofit2.http.Query
import com.imhungry.sillok.data.model.folder.MeetingFolderUpdateReqDto
import com.imhungry.sillok.data.model.folder.MeetingFolderUpdateResDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface MeetingFolderApi {
    @POST("/api/v1/meeting-folders")
    suspend fun createFolder(
        @Body request: MeetingFolderCreationReqDto
    ): Response<MeetingFolderCreationResDto>

    @HTTP(method = "DELETE", path = "/api/v1/meeting-folders", hasBody = true)
    suspend fun deleteFolders(
        @Body request: MeetingFolderDeletionReqDto
    ): Response<MeetingFolderDeletionResDto>

    @GET("/api/v1/meeting-folders")
    suspend fun getAllFolders(
        @Query("name") name: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<MeetingFolderListResDto>

    @GET("/api/v1/meeting-folders/{folderId}/meetings")
    suspend fun getFolderMeetings(
        @Path("folderId") folderId: Long
    ): Response<MeetingFolderDetailResDto>

    @POST("/api/v1/meeting-folders/{folderId}/meetings")
    suspend fun addMeetings(
        @Path("folderId") folderId: Long,
        @Body request: MeetingFolderUpdateReqDto
    ): Response<MeetingFolderUpdateResDto>

    @HTTP(method = "DELETE", path = "/api/v1/meeting-folders/{folderId}/meetings", hasBody = true)
    suspend fun removeMeetings(
        @Path("folderId") folderId: Long,
        @Body request: MeetingFolderUpdateReqDto
    ): Response<MeetingFolderUpdateResDto>
}

