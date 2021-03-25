package com.xyoye.local_component.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.RequestError
import com.xyoye.common_component.network.request.RequestErrorHandler
import com.xyoye.data_component.data.SubtitleSearchData
import com.xyoye.data_component.data.SubtitleSubData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flatMapLatest
import retrofit2.HttpException

/**
 * Created by xyoye on 2021/3/25.
 */

class SearchSubtitleRepository(private val scope: CoroutineScope) {

    private val searchKeyLiveData = MutableLiveData<String>()

    val subtitleLiveData = searchKeyLiveData.asFlow()
        .flatMapLatest {
            getPager(it).flow.cachedIn(scope)
        }.asLiveData()

    fun search(keyword: String) {
        searchKeyLiveData.postValue(keyword)
    }

    private fun getPager(keyword: String): Pager<Int, SubtitleSearchData> {
        return Pager(
            config = PagingConfig(15, 15),
            pagingSourceFactory = { SearchSubtitleSource(keyword) }
        )
    }

    private inner class SearchSubtitleSource(private val keyword: String) :
        PagingSource<Int, SubtitleSearchData>() {

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SubtitleSearchData> {
            if (params is LoadParams.Prepend) {
                return LoadResult.Page(
                    data = listOf(),
                    prevKey = null,
                    nextKey = null
                )
            }

            val page = if (params.key == null) 1 else params.key as Int

            return try {
                val shooterSecret = SubtitleConfig.getShooterSecret() ?: ""
                val subData =
                    Retrofit.extService.searchSubtitle(shooterSecret, keyword, page)
                LoadResult.Page(sub2SubtitleSearchData(subData), null, page + 1)
            } catch (e: Exception) {
                //处理509异常
                if (e is HttpException && e.code() == 509) {
                    val limitError = RequestError(509, "请求频率过高")
                    LoadResult.Error(limitError)
                } else {
                    LoadResult.Error(RequestErrorHandler(e).handlerError())
                }
            }
        }

        /**
         * 搜索字幕转显示数据类型
         */
        private fun sub2SubtitleSearchData(subData: SubtitleSubData?): MutableList<SubtitleSearchData> {
            return mutableListOf<SubtitleSearchData>().apply {
                val subList = subData?.sub?.subs
                if (subList?.size ?: 0 > 0) {
                    for (subDetailData in subList!!) {
                        val subtitleName =
                            if (subDetailData.native_name.isNullOrEmpty())
                                subDetailData.videoname
                            else
                                subDetailData.native_name
                        add(
                            SubtitleSearchData(
                                subDetailData.id,
                                subtitleName,
                                subDetailData.upload_time,
                                subDetailData.subtype,
                                subDetailData.lang?.desc
                            )
                        )
                    }
                }
            }
        }
    }
}