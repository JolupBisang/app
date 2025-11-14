package com.imhungry.sillok.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.feedback.Feedback
import com.imhungry.sillok.domain.usecase.feedback.GetFeedbacksUseCase

class FeedbackPagingSource(
    private val getFeedbacksUseCase: GetFeedbacksUseCase,
    private val meetingId: Long,
    private val pageSize: Int = 500
) : PagingSource<Int, Feedback>() {

    companion object {
        private const val TAG = "FeedbackPagingSource"
        private const val INITIAL_PAGE = 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Feedback> {
        return try {
            val page = params.key ?: INITIAL_PAGE
            val loadSize = params.loadSize.coerceAtMost(pageSize)

            Log.d(TAG, "load: page=$page, loadSize=$loadSize, meetingId=$meetingId")

            when (val result = getFeedbacksUseCase(meetingId, page, loadSize)) {
                is ApiResult.Success -> {
                    val feedbacks = result.data

                    Log.d(TAG, "load 성공: feedbacks.size=${feedbacks.size}")

                    LoadResult.Page(
                        data = feedbacks,
                        prevKey = if (page == INITIAL_PAGE) null else page - 1,
                        nextKey = if (feedbacks.size < loadSize) null else page + 1
                    )
                }
                is ApiResult.Failure -> {
                    Log.e(TAG, "load 실패: ${result.message}")
                    LoadResult.Error(Exception(result.message ?: "피드백 로드 실패"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "load 예외 발생: ${e.message}", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Feedback>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

