package com.imhungry.sillok.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.summary.Summary
import com.imhungry.sillok.domain.usecase.summary.GetSummariesUseCase

class SummaryPagingSource(
    private val getSummariesUseCase: GetSummariesUseCase,
    private val meetingId: Long,
    private val isRecap: Boolean = false,
    private val pageSize: Int = 500
) : PagingSource<Int, Summary>() {

    companion object {
        private const val TAG = "SummaryPagingSource"
        private const val INITIAL_PAGE = 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Summary> {
        return try {
            val page = params.key ?: INITIAL_PAGE
            val loadSize = params.loadSize.coerceAtMost(pageSize)

            Log.d(TAG, "load: page=$page, loadSize=$loadSize, meetingId=$meetingId, isRecap=$isRecap")

            when (val result = getSummariesUseCase(meetingId, isRecap, page, loadSize)) {
                is ApiResult.Success -> {
                    val summaries = result.data

                    Log.d(TAG, "load 성공: summaries.size=${summaries.size}")

                    LoadResult.Page(
                        data = summaries,
                        prevKey = if (page == INITIAL_PAGE) null else page - 1,
                        nextKey = if (summaries.size < loadSize) null else page + 1
                    )
                }
                is ApiResult.Failure -> {
                    Log.e(TAG, "load 실패: ${result.message}")
                    LoadResult.Error(Exception(result.message ?: "요약 로드 실패"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "load 예외 발생: ${e.message}", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Summary>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

