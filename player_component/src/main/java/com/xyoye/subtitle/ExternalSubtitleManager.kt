package com.xyoye.subtitle

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleCoroutineScope
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.subtitle.exception.FatalParsingException
import com.xyoye.subtitle.format.FormatFactory
import com.xyoye.subtitle.info.TimedTextObject
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.min

/**
 * Created by xyoye on 2020/12/14.
 *
 * 外挂字幕控制器
 */

class ExternalSubtitleManager {

    companion object {
        private const val UPDATE_SUBTITLE_MSG = 0x1
        private const val UPDATE_SUBTITLE_INTERVAL_MS = 500L
    }

    private lateinit var lifecycleScope: LifecycleCoroutineScope
    private lateinit var output: SubtitleOutput
    private lateinit var handler: Handler
    private var onSubtitleLoaded: ((sourceUrl: String, isLoaded: Boolean) -> Unit)? = null

    private var offsetTimeMs: Long = 0L
    private var mTimedTextObject: TimedTextObject? = null

    private val handlerCallback = Handler.Callback {
        if (it.what == UPDATE_SUBTITLE_MSG) {
            //停止前发送一个空字幕
            if (!mRunning) {
                output.onSubtitleOutput(arrayListOf())
                mStopped = true
                return@Callback true
            }

            val curPosition = output.getCurrentPosition() + offsetTimeMs
            output.onSubtitleOutput(findSubtitle(curPosition))
            handler.apply {
                sendMessageDelayed(obtainMessage(UPDATE_SUBTITLE_MSG), UPDATE_SUBTITLE_INTERVAL_MS)
            }
        }
        return@Callback true
    }

    @Volatile
    private var mRunning = false

    @Volatile
    private var mStopped = true

    fun bindOutput(lifecycleScope: LifecycleCoroutineScope, output: SubtitleOutput) {
        this.lifecycleScope = lifecycleScope
        this.output = output
        handler = Handler(Looper.getMainLooper(), handlerCallback)
    }

    fun bindSource(subtitlePath: String, playWhenReady: Boolean = false) {
        lifecycleScope.launch(context = Dispatchers.Main) {
            mTimedTextObject = lifecycleScope.async(Dispatchers.IO, start = CoroutineStart.LAZY) {
                parserSource(subtitlePath)
            }.await()

            if (mTimedTextObject != null && playWhenReady){
                start()
            }
            onSubtitleLoaded?.invoke(subtitlePath, mTimedTextObject != null)
        }
    }

    fun start() {
        if (mRunning)
            return
        if (!mRunning && !mStopped) {
            mRunning = true
            return
        }
        mRunning = true
        handler.sendEmptyMessage(UPDATE_SUBTITLE_MSG)
    }

    fun stop() {
        mRunning = false
    }

    fun setOffset(offsetMs: Long) {
        offsetTimeMs = offsetMs
        if (mRunning)
            handler.sendEmptyMessage(UPDATE_SUBTITLE_MSG)
    }

    fun observerOnSubtitleLoad(loadedCallback: ((sourceUrl: String, isLoaded: Boolean) -> Unit)?) {
        this.onSubtitleLoaded = loadedCallback
    }

    /**
     * 在所有字幕中找当前时间的字幕
     */
    private fun findSubtitle(position: Long): MutableList<SubtitleText> {
        val subtitleList = mutableListOf<SubtitleText>()
        if (mTimedTextObject == null)
            return subtitleList

        //字幕初始时间
        val minMs: Long = mTimedTextObject!!.captions.firstKey()
        //字幕结束时间
        val maxMs: Long = mTimedTextObject!!.captions.lastKey()

        //当前进度未达字幕初始时间
        if (position < minMs || minMs > maxMs)
            return subtitleList

        //取当前进度前十秒
        val startMs = max(minMs, position - 10 * 1000L)
        //取当前进度后十秒
        val endMs = min(maxMs, position + 10 * 1000L)

        //当字幕与视频不匹配时，进度-10s任然会大于最大进度
        if (startMs > endMs) {
            return subtitleList
        }

        //获取二十秒间所有字幕
        val subtitleCaptions = mTimedTextObject!!.captions.subMap(startMs, endMs)

        //遍历字幕，取当前时间字幕
        for (caption in subtitleCaptions.values) {
            val captionStartMs = caption.start.getMseconds()
            val captionEndMs = caption.end.getMseconds()

            //1ms容错
            if (position < captionStartMs - 1) {
                break
            }

            //1ms容错
            if (position >= captionStartMs - 1L && position <= captionEndMs) {
                subtitleList.addAll(SubtitleUtils.caption2Subtitle(caption))
            }
        }


        return subtitleList
    }


    private fun parserSource(subtitlePath: String): TimedTextObject? {
        try {
            if (subtitlePath.isNotEmpty()) {
                //解析字幕文件
                val subtitleFile = File(subtitlePath)
                if (subtitleFile.exists()) {
                    val format = FormatFactory.findFormat(subtitlePath)
                    if (format == null) {
                        ToastCenter.showOriginalToast("不支持的外挂字幕格式")
                        return null
                    }
                    val subtitleObj = format.parseFile(subtitleFile)
                    if (subtitleObj.captions.size == 0) {
                        ToastCenter.showOriginalToast("外挂字幕内容为空")
                        return null
                    }
                    return subtitleObj
                }
            }
        } catch (e: FatalParsingException) {
            e.printStackTrace()
            ToastCenter.showOriginalToast("解析外挂字幕文件失败")
        }
        return null
    }
}