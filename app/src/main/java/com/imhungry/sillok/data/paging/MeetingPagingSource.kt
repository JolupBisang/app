package com.imhungry.sillok.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.domain.usecase.meeting.SearchMeetingsUseCase

class MeetingPagingSource(
    private val searchMeetingsUseCase: SearchMeetingsUseCase,
    private val query: String
) : PagingSource<Int, MeetingDetailSummary>() {

    companion object {
        private const val TAG = "MeetingPagingSource"
        private const val INITIAL_PAGE = 0
        private const val PAGE_SIZE = 20
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MeetingDetailSummary> {
        return try {
            val page = params.key ?: INITIAL_PAGE
            val pageSize = params.loadSize.coerceAtMost(PAGE_SIZE)

            Log.d(TAG, "load: page=$page, pageSize=$pageSize, query=$query")

            when (val result = searchMeetingsUseCase(query, page, pageSize)) {
                is ApiResult.Success -> {
                    val slice = result.data
                    val meetings = slice.content

                    Log.d(TAG, "load 성공: meetings.size=${meetings.size}, hasNext=${slice.hasNext}")

                    LoadResult.Page(
                        data = meetings,
                        prevKey = if (page == INITIAL_PAGE) null else page - 1,
                        nextKey = if (slice.hasNext) page + 1 else null
                    )
                }
                is ApiResult.Failure -> {
                    Log.e(TAG, "load 실패: ${result.message}")
                    LoadResult.Error(Exception(result.message ?: "검색 실패"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "load 예외 발생: ${e.message}", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MeetingDetailSummary>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

