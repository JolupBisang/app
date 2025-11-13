package com.imhungry.sillok.data.repository.folder

import com.imhungry.sillok.data.mapper.folder.MeetingFolderMapper
import com.imhungry.sillok.data.remote.folder.MeetingFolderApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.folder.AddMeetingsToFolderRequest
import com.imhungry.sillok.domain.model.folder.CreateMeetingFolderRequest
import com.imhungry.sillok.domain.model.folder.DeleteMeetingFoldersRequest
import com.imhungry.sillok.domain.model.folder.MeetingFolderDetail
import com.imhungry.sillok.domain.model.folder.MeetingFolderList
import com.imhungry.sillok.domain.model.folder.RemoveMeetingsFromFolderRequest
import com.imhungry.sillok.domain.repository.folder.MeetingMinutesFolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MeetingFolderRepositoryImpl @Inject constructor(
    private val api: MeetingFolderApi,
    private val mapper: MeetingFolderMapper
) : MeetingMinutesFolderRepository {
    override suspend fun createFolder(request: CreateMeetingFolderRequest): ApiResult<Long> =
        withContext(Dispatchers.IO) {
            try {
                val dto = mapper.toDto(request)
                val res = api.createFolder(dto)
                if (res.isSuccessful) {
                    ApiResult.Success(res.body()?.folderId ?: -1L)
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }

    override suspend fun deleteFolders(request: DeleteMeetingFoldersRequest): ApiResult<List<Long>> =
        withContext(Dispatchers.IO) {
            try {
                val dto = mapper.toDto(request)
                val res = api.deleteFolders(dto)
                if (res.isSuccessful) {
                    ApiResult.Success(res.body()?.successFolderIds ?: emptyList())
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }

    override suspend fun getAllFolders(): ApiResult<MeetingFolderList> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getAllFolders()
                if (res.isSuccessful) {
                    val dto = res.body()
                    if (dto != null) {
                        ApiResult.Success(mapper.toDomain(dto))
                    } else {
                        ApiResult.Failure("응답 데이터가 없습니다.")
                    }
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }

    override suspend fun getFolderMeetings(folderId: Long): ApiResult<MeetingFolderDetail> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getFolderMeetings(folderId)
                if (res.isSuccessful) {
                    val dto = res.body()
                    if (dto != null) {
                        ApiResult.Success(mapper.toDomain(dto))
                    } else {
                        ApiResult.Failure("응답 데이터가 없습니다.")
                    }
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }

    override suspend fun addMeetingsToFolder(request: AddMeetingsToFolderRequest): ApiResult<List<Long>> =
        withContext(Dispatchers.IO) {
            try {
                val dto = mapper.toDto(request)
                val res = api.addMeetings(request.folderId, dto)
                if (res.isSuccessful) {
                    ApiResult.Success(res.body()?.successMeetingIds ?: emptyList())
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }

    override suspend fun removeMeetingsFromFolder(request: RemoveMeetingsFromFolderRequest): ApiResult<List<Long>> =
        withContext(Dispatchers.IO) {
            try {
                val dto = mapper.toDto(request)
                val res = api.removeMeetings(request.folderId, dto)
                if (res.isSuccessful) {
                    ApiResult.Success(res.body()?.successMeetingIds ?: emptyList())
                } else {
                    ApiResult.Failure(res.message())
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
            }
        }
}

