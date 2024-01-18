package com.xyoye.common_component.utils.subtitle

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.ifNullOrBlank
import com.xyoye.common_component.network.repository.ResourceRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.network.request.dataOrNull
import com.xyoye.data_component.data.SubtitleSourceBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Created by xyoye on 2021/3/25.
 */

class SubtitleSearchHelper(private val scope: CoroutineScope) {

    private val searchKeyLiveData = MutableLiveData<String>()

    val subtitleLiveData = searchKeyLiveData.asFlow()
        .flatMapLatest {
            getPager(it).flow.cachedIn(scope)
        }.asLiveData()

    fun search(keyword: String) {
        searchKeyLiveData.postValue(keyword)
    }

    private fun getPager(keyword: String): Pager<Int, SubtitleSourceBean> {
        return Pager(
            config = PagingConfig(15, 15),
            pagingSourceFactory = { SearchSubtitleSource(keyword) }
        )
    }

    private inner class SearchSubtitleSource(private val keyword: String) :
        PagingSource<Int, SubtitleSourceBean>() {

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SubtitleSourceBean> {
            if (params is LoadParams.Prepend) {
                return LoadResult.Page(
                    data = listOf(),
                    prevKey = null,
                    nextKey = null
                )
            }

            val page = if (params.key == null) 1 else params.key as Int

            val shooterSecret = SubtitleConfig.getShooterSecret() ?: ""
            val result = ResourceRepository.searchSubtitle(shooterSecret, keyword, page)
            if (result is Response.Error) {
                return if (result.error.code == 509) {
                    LoadResult.Error(result.error.copy(msg = "请求频率过高"))
                } else {
                    LoadResult.Error(result.error)
                }
            }

            val subtitleData = result.dataOrNull?.sub?.subs?.map {
                SubtitleSourceBean(
                    it.id,
                    it.native_name.ifNullOrBlank { it.videoname.orEmpty() },
                    it.upload_time,
                    it.subtype,
                    it.lang?.desc
                )
            } ?: emptyList()

            return LoadResult.Page(subtitleData, null, page + 1)
        }

        override fun getRefreshKey(state: PagingState<Int, SubtitleSourceBean>): Int? {
            return null
        }
    }
}