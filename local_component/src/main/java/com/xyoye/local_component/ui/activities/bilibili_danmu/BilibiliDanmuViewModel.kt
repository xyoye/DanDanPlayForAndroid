package com.xyoye.local_component.ui.activities.bilibili_danmu

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.config.Api
import com.xyoye.common_component.network.repository.OtherRepository
import com.xyoye.common_component.network.repository.ResourceRepository

import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.danmu.DanmuFinder
import com.xyoye.data_component.bean.LocalDanmuBean
import com.xyoye.data_component.data.AnimeCidData
import com.xyoye.data_component.data.DanmuEpisodeData
import com.xyoye.data_component.data.EpisodeCidData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.File
import java.util.regex.Pattern

class BilibiliDanmuViewModel : BaseViewModel() {

    val downloadMessageLiveData = MutableLiveData<String>()
    val clearMessageLiveData = MutableLiveData<Boolean>()

    private val gzipHeader = mapOf(Pair("Accept-Encoding", "gzip,deflate"))

    fun downloadByCode(code: String, isAvCode: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            clearDownloadMessage()

            val mode = if (isAvCode) "AV号" else "BV号"
            sendDownloadMessage("以${mode}模式下载：$code")

            actionDo("开始获取CID")
            val episodeCid = OtherRepository.getCidInfo(isAvCode, code).getOrNull()?.data
            if (episodeCid == null) {
                actionFailed()
                return@launch
            }
            actionSuccess(episodeCid.cid)

            saveEpisodeDanmu(episodeCid)
        }
    }

    fun downloadByUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            clearDownloadMessage()
            val isBangumiUrl = Uri.parse(url).pathSegments.firstOrNull() == "bangumi"
            if (isBangumiUrl) {
                downloadByBangumiUrl(url)
            } else {
                downloadByVideoUrl(url)
            }
        }
    }

    private suspend fun downloadByBangumiUrl(url: String) {
        sendDownloadMessage("以视频链接模式下载：$url")

        actionDo("获取番剧剧集列表")
        val animeCid = findAnimeCidInJavaScript(url)
        if (animeCid == null || animeCid.episodes.isEmpty()) {
            actionFailed()
            return
        }
        actionSuccess("共${animeCid.episodes.size}集")

        actionDo("开始下载弹幕")
        sendDownloadMessage("========================")
        val resultList = animeCid.episodes.map {
            saveEpisodeDanmu(it, animeCid.animeTitle)
        }
        sendDownloadMessage("------------------------")
        sendDownloadMessage("========================")

        val successCount = resultList.count { it != null }
        val failedCount = resultList.size - successCount
        sendDownloadMessage("")
        sendDownloadMessage("番剧剧集弹幕下载完成，成功：$successCount, 失败 :$failedCount")

        if (successCount > 0) {
            val dirPath = File(PathHelper.getDanmuDirectory(), animeCid.animeTitle).absolutePath
            sendDownloadMessage("弹幕已保存目录：$dirPath")
        }
    }

    private suspend fun downloadByVideoUrl(url: String) {
        sendDownloadMessage("以视频链接模式下载：$url")

        actionDo("获取视频CID")
        val episodeCid = findVideoCidInJavaScript(url)
        if (episodeCid == null) {
            actionFailed()
            return
        }
        actionSuccess(episodeCid.cid)

        saveEpisodeDanmu(episodeCid)
    }

    private suspend fun saveEpisodeDanmu(episodeCid: EpisodeCidData) {
        actionDo("开始下载弹幕")
        sendDownloadMessage("========================")
        val localDanmu = saveEpisodeDanmu(episodeCid, "")
        sendDownloadMessage("------------------------")
        sendDownloadMessage("========================")

        if (localDanmu != null) {
            sendDownloadMessage("")
            sendDownloadMessage("弹幕文件已保存至：")
            sendDownloadMessage(localDanmu.danmuPath)
        }
    }

    private suspend fun saveEpisodeDanmu(episodeCid: EpisodeCidData, animeTitle: String): LocalDanmuBean? {
        val url = "${Api.BILI_BILI_COMMENT}${episodeCid.cid}.xml"
        sendDownloadMessage("------------------------")
        sendDownloadMessage("剧集：${episodeCid.title}")

        actionDo("获取弹幕内容")
        val inputStream = ResourceRepository.getResourceResponseBody(url, gzipHeader).getOrNull()?.byteStream()
        if (inputStream == null) {
            actionFailed()
            return null
        }
        actionSuccess()

        actionDo("保存弹幕内容")
        val danmuEpisode = DanmuEpisodeData(animeTitle = animeTitle, episodeTitle = episodeCid.title)
        val localDanmu = DanmuFinder.instance.saveStream(danmuEpisode, inputStream)
        if (localDanmu == null) {
            actionFailed()
            return null
        }
        actionSuccess()
        return localDanmu
    }

    private suspend fun findVideoCidInJavaScript(url: String): EpisodeCidData? {
        val htmlElement = try {
            Jsoup.connect(url).timeout(10 * 1000).get().toString()
        } catch (t: Throwable) {
            t.printStackTrace()
            sendDownloadMessage("错误：${t.message}")
            return null
        }
        val header = "__INITIAL_STATE__="
        val footer = ";(function"
        val footerRegex = footer.replace("(", "\\(")

        val pattern = Pattern.compile("($header).*($footerRegex)")
        val matcher = pattern.matcher(htmlElement)
        if (matcher.find().not()) {
            return null
        }

        val jsonObject = try {
            JSONObject(matcher.group(0)?.removeSurrounding(header, footer).orEmpty())
        } catch (e: Exception) {
            return null
        }

        val videoJson = jsonObject.optJSONObject("videoData")
            ?: return null

        val cid = videoJson.optString("cid") ?: return null
        val episodeTitle = videoJson.optString("title")

        return EpisodeCidData(episodeTitle, cid)
    }

    private suspend fun findAnimeCidInJavaScript(url: String): AnimeCidData? {
        val htmlElement = try {
            Jsoup.connect(url).timeout(10 * 1000).get().toString()
        } catch (t: Throwable) {
            t.printStackTrace()
            sendDownloadMessage("错误：${t.message}")
            return null
        }

        val header = "<script id=\"__NEXT_DATA__\" type=\"application/json\">"
        val footer = "</script>"

        val pattern = Pattern.compile("($header).*($footer)")
        val matcher = pattern.matcher(htmlElement)
        if (matcher.find().not()) {
            return null
        }

        val jsonObject = try {
            JSONObject(matcher.group(0)?.removeSurrounding(header, footer).orEmpty())
        } catch (e: Exception) {
            return null
        }

        val mediaInfoJson = jsonObject
            .optJSONObject("props")
            ?.optJSONObject("pageProps")
            ?.optJSONObject("dehydratedState")
            ?.optJSONArray("queries")
            ?.optJSONObject(0)
            ?.optJSONObject("state")
            ?.optJSONObject("data")
            ?.optJSONObject("seasonInfo")
            ?.optJSONObject("mediaInfo")
            ?: return null

        val animeTitle = mediaInfoJson.optString("title").orEmpty()
        val episodesJson = mediaInfoJson.optJSONArray("episodes")
            ?: return null

        val episodeList = mutableListOf<EpisodeCidData>()
        for (index in 0 until episodesJson.length()) {
            val episodeJson = episodesJson.optJSONObject(index)
            val title = episodeJson.optString("playerEpTitle").orEmpty()
            val cid = episodeJson.optString("cid") ?: continue
            episodeList.add(EpisodeCidData(title, cid))
        }
        return AnimeCidData(animeTitle, episodeList)
    }

    private suspend fun actionDo(name: String) {
        sendDownloadMessage("")
        sendDownloadMessage("操作：${name}")
    }

    private suspend fun actionFailed() {
        sendDownloadMessage("结果：失败")
    }

    private suspend fun actionSuccess(extra: String? = null) {
        val display = if (extra.isNullOrEmpty()) "" else "，$extra"
        sendDownloadMessage("结果：成功$display")
    }

    private suspend fun sendDownloadMessage(message: String) {
        withContext(Dispatchers.Main) {
            downloadMessageLiveData.value = message
        }
    }

    private fun clearDownloadMessage() {
        clearMessageLiveData.postValue(true)
    }
}