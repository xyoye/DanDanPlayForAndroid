package com.xyoye.local_component.ui.activities.bilibili_danmu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.data_component.bean.BungumiCidBean
import com.xyoye.data_component.bean.VideoCidBean
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

class BilibiliDanmuViewModel : BaseViewModel() {

    val downloadMessageLiveData = MutableLiveData<String>()
    val clearMessageLiveData = MutableLiveData<Boolean>()

    fun downloadByCode(code: String, isAvCode: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            clearDownloadMessage()

            val message = if (isAvCode) "以AV号模式下载, AV号：$code" else "以BV号模式下载, BV号：$code"

            sendDownloadMessage("$message\n\n开始获取CID")
            val cidInfo = getCodeCid(isAvCode, code)
            if (cidInfo == null) {
                sendDownloadMessage("获取CID失败")
                return@launch
            }

            sendDownloadMessage("获取CID成功\n\n开始获取弹幕内容")
            val xmlContent = getXmlContentByCid(cidInfo.first)
            if (xmlContent.isNullOrEmpty()) {
                sendDownloadMessage("获取弹幕内容失败")
                return@launch
            }

            sendDownloadMessage("获取弹幕内容成功\n\n开始保存弹幕内容")
            val danmuFileName = cidInfo.second + ".xml"
            val danmuPath = DanmuUtils.saveDanmu(danmuFileName, null, xmlContent)
            if (danmuPath.isNullOrEmpty()) {
                sendDownloadMessage("保存弹幕内容失败")
                return@launch
            }
            sendDownloadMessage("保存弹幕内容成功\n\n文件已保存至：$danmuPath")
        }
    }

    fun downloadByUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            clearDownloadMessage()

            val message = "以视频链接模式下载, 视频链接：$url"

            if (isBangumiUrl(url)) {
                sendDownloadMessage("$message\n\n开始获取番剧列表CID")
                val cidInfo = getBungumiCid(url)
                if (cidInfo == null) {
                    sendDownloadMessage("获取番剧列表CID失败")
                    return@launch
                }
                sendDownloadMessage("获取番剧列表CID成功, CID数量：${cidInfo.first.size}\n\n开始下载弹幕")

                for ((index, cid) in cidInfo.first.withIndex()) {
                    var downloadMessage = "第${index + 1}集  "
                    val xmlContent = getXmlContentByCid(cid)
                    if (xmlContent == null) {
                        downloadMessage += "获取弹幕内容失败"
                        sendDownloadMessage(downloadMessage)
                        continue
                    }
                    downloadMessage += "获取弹幕内容成功  "
                    val fileName = "${index + 1}.xml"
                    val danmuPath = DanmuUtils.saveDanmu(fileName, cidInfo.second, xmlContent)
                    if (danmuPath == null) {
                        downloadMessage += "保存弹幕失败"
                        sendDownloadMessage(downloadMessage)
                        continue
                    }
                    downloadMessage += "保存弹幕成功"
                    sendDownloadMessage(downloadMessage)
                }
                val danmuFolder = File(PathHelper.getDanmuDirectory(), cidInfo.second).absolutePath
                sendDownloadMessage("\n番剧列表弹幕下载完成\n文件已保存至：$danmuFolder\"")
            } else {
                sendDownloadMessage("$message\n\n开始获取视频CID")
                val cidInfo = getVideoCid(url)
                if (cidInfo == null) {
                    sendDownloadMessage("获取视频CID失败")
                    return@launch
                }

                sendDownloadMessage("获取视频CID成功\n\n开始获取弹幕内容")
                val xmlContent = getXmlContentByCid(cidInfo.first)
                if (xmlContent.isNullOrEmpty()) {
                    sendDownloadMessage("获取弹幕内容失败")
                    return@launch
                }

                sendDownloadMessage("获取弹幕内容成功\n\n开始保存弹幕内容")
                val danmuFileName = cidInfo.second + ".xml"
                val danmuPath = DanmuUtils.saveDanmu(danmuFileName, null, xmlContent)
                if (danmuPath.isNullOrEmpty()) {
                    sendDownloadMessage("保存弹幕内容失败")
                    return@launch
                }
                sendDownloadMessage("保存弹幕内容成功\n\n文件已保存至：$danmuPath")
            }
        }
    }

    private suspend fun getVideoCid(url: String): Pair<Long, String>? {
        return viewModelScope.async(Dispatchers.IO) {

            try {
                val htmlElement = Jsoup.connect(url).timeout(10 * 1000).get().toString()
                //匹配java script里的json数据
                val pattern = Pattern.compile("(__INITIAL_STATE__=).*(;\\(function)")
                val matcher = pattern.matcher(htmlElement)
                if (matcher.find()) {
                    var jsonText = matcher.group(0) ?: return@async null
                    if (jsonText.isNotEmpty()) {
                        jsonText = jsonText.substring(18)
                        jsonText = jsonText.substring(0, jsonText.length - 10)
                        val cidBean =
                            JsonHelper.parseJson<VideoCidBean>(jsonText) ?: return@async null
                        //只需要标题和cid
                        return@async Pair(cidBean.videoData.cid, cidBean.videoData.title)
                    }
                }
            } catch (t: Throwable) {
                sendDownloadMessage("错误：${t.message}")
                t.printStackTrace()
            }
            null
        }.await()
    }

    private suspend fun getBungumiCid(url: String): Pair<MutableList<Long>, String>? {
        return viewModelScope.async(Dispatchers.IO, start = CoroutineStart.LAZY) {
            try {
                val htmlElement = Jsoup.connect(url).timeout(10 * 1000).get().toString()
                val pattern = Pattern.compile("(__INITIAL_STATE__=).*(;\\(function)")
                val matcher = pattern.matcher(htmlElement)
                if (matcher.find()) {
                    var jsonText = matcher.group(0) ?: return@async null
                    if (jsonText.isNotEmpty()) {
                        jsonText = jsonText.substring(18)
                        jsonText = jsonText.substring(0, jsonText.length - 10)
                        val cidBean =
                            JsonHelper.parseJson<BungumiCidBean>(jsonText) ?: return@async null
                        //只需要标题和cid
                        val cidList = mutableListOf<Long>()
                        cidBean.epList.forEach {
                            cidList.add(it.cid)
                        }
                        return@async Pair(cidList, cidBean.mediaInfo.title)
                    }
                }
            } catch (t: Throwable) {
                sendDownloadMessage("错误：${t.message}")
                t.printStackTrace()
            }
            null
        }.await()

    }

    private suspend fun getCodeCid(isAvCode: Boolean, value: String): Pair<Long, String>? {
        return viewModelScope.async(Dispatchers.IO, start = CoroutineStart.LAZY) {
            val key = if (isAvCode) "aid" else "bvid"
            val apiUrl = "https://api.bilibili.com/x/web-interface/view?$key=$value"
            try {
                val cidData = Retrofit.extService.getCidInfo(apiUrl)
                if (cidData.code == 0 && cidData.data != null) {
                    val videoTitle = cidData.data!!.title ?: "未知弹幕"
                    return@async Pair(cidData.data!!.cid, videoTitle)
                }
            } catch (t: Throwable) {
                sendDownloadMessage("错误：${t.message}")
                t.printStackTrace()
            }
            null
        }.await()
    }

    private suspend fun getXmlContentByCid(cid: Long): String? {
        return viewModelScope.async(Dispatchers.IO, start = CoroutineStart.LAZY) {
            val url = "http://comment.bilibili.com/$cid.xml"
            val header = mapOf(Pair("Accept-Encoding", "gzip,deflate"))

            var inputStream: InputStream? = null
            var inflaterInputStream: InflaterInputStream? = null
            var scanner: Scanner? = null

            var xmlContent: String? = null
            try {
                val responseBody = Retrofit.extService.downloadResource(url, header)

                inputStream = responseBody.byteStream()
                inflaterInputStream = InflaterInputStream(inputStream, Inflater(true))
                scanner = Scanner(inflaterInputStream, Charsets.UTF_8.name())

                val contentBuilder = StringBuilder()
                while (scanner.hasNext()) {
                    contentBuilder.append(scanner.nextLine())
                }

                xmlContent = contentBuilder.toString()
            } catch (e: Throwable) {
                sendDownloadMessage("错误：${e.message}")
                e.printStackTrace()
            } finally {
                IOUtils.closeIO(scanner)
                IOUtils.closeIO(inflaterInputStream)
                IOUtils.closeIO(inputStream)
            }
            xmlContent
        }.await()
    }

    private fun isBangumiUrl(url: String): Boolean {
        return url.contains("www.bilibili.com/bangumi") || url.contains("m.bilibili.com/bangumi")
    }

    private fun sendDownloadMessage(message: String) {
        downloadMessageLiveData.postValue(message)
    }

    private fun clearDownloadMessage() {
        clearMessageLiveData.postValue(true)
    }
}