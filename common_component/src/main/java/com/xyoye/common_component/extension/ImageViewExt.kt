package com.xyoye.common_component.extension

import android.net.Uri
import android.widget.ImageView
import coil.load
import coil.request.CachePolicy
import coil.request.videoFramePercent
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.xyoye.common_component.R
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.coil.CoilLoadCompleteTarget
import com.xyoye.common_component.utils.coil.CoilPaletteTarget
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.isNetworkScheme
import java.io.File

/**
 * Created by xyoye on 2020/7/31.
 */

fun ImageView.loadImage(source: String?) {
    load(source) {
        scale(Scale.FILL)
        crossfade(true)
    }
}

fun ImageView.loadImage(source: String?, dpRadius: Int) {
    val radius = dp2px(dpRadius).toFloat()
    val transformation = RoundedCornersTransformation(radius)
    load(source) {
        scale(Scale.FILL)
        crossfade(true)
        transformations(transformation)
    }
}

fun ImageView.loadImageWithPalette(source: String?, onPaletteColor: (Int) -> Unit) {
    load(source) {
        scale(Scale.FILL)
        crossfade(true)
        allowHardware(false)
        target(CoilPaletteTarget(this@loadImageWithPalette, onPaletteColor))
    }
}

fun ImageView.loadImageWithCallback(
    source: String?,
    dpRadius: Float = 0f,
    errorRes: Int = 0,
    onComplete: () -> Unit
) {
    val transformation = if (dpRadius > 0)
        RoundedCornersTransformation(dpRadius.dp())
    else
        null

    load(source) {
        scale(Scale.FILL)
        crossfade(true)
        error(errorRes)
        transformation?.let {
            transformations(it)
        }
        target(CoilLoadCompleteTarget(this@loadImageWithCallback, onComplete))
    }
}

fun ImageView.loadImageByKey(uniqueKey: String?) {
    load(uniqueKey.toCoverFile()) {
        scale(Scale.FILL)
        crossfade(true)
        error(R.drawable.ic_dandanplay)
        transformations(RoundedCornersTransformation(5f.dp()))
        diskCachePolicy(CachePolicy.DISABLED)
        memoryCachePolicy(CachePolicy.DISABLED)
        videoFramePercent(0.1)
    }
}

fun ImageView.loadImage(file: StorageFile) {
    val source = getImageSource(file)
    val cachePolicy = if (source is File)
        CachePolicy.DISABLED
    else
        CachePolicy.ENABLED

    load(source) {
        scale(Scale.FILL)
        crossfade(true)
        error(R.drawable.ic_dandanplay)
        transformations(RoundedCornersTransformation(5f.dp()))
        diskCachePolicy(cachePolicy)
        memoryCachePolicy(cachePolicy)
        videoFramePercent(0.1)
    }
}

@JvmName("-deprecated_uri")
@Deprecated(
    message = "moved to loadImage()",
    replaceWith = ReplaceWith(expression = "loadImage()"),
    level = DeprecationLevel.WARNING
)
fun ImageView.setVideoCover(uniqueKey: String?, placeholder: String? = null) {
    val coverFile = uniqueKey.toCoverFile()
    var diskCachePolicy = CachePolicy.ENABLED
    var memoryCachePolicy = CachePolicy.ENABLED

    var image: Any? = null
    if (coverFile.isValid()) {
        diskCachePolicy = CachePolicy.DISABLED
        memoryCachePolicy = CachePolicy.DISABLED
        image = coverFile
    }
    if (image == null && placeholder != null) {
        val isNetworkUrl = isNetworkScheme(Uri.parse(placeholder).scheme)
        if (isNetworkUrl.not()) {
            image = placeholder
        }
    }
    image = image ?: R.drawable.ic_dandanplay

    val radius = dp2px(5).toFloat()
    val transformation = RoundedCornersTransformation(radius)
    load(image) {
        scale(Scale.FILL)
        crossfade(true)
        error(R.drawable.ic_dandanplay)
        transformations(transformation)
        diskCachePolicy(diskCachePolicy)
        memoryCachePolicy(memoryCachePolicy)
        videoFramePercent(0.1)
    }
}

private fun getImageSource(file: StorageFile): Any {
    var source: Any? = null
    val coverFile = file.uniqueKey().toCoverFile()
    //视频封面缓存文件有效
    if (coverFile.isValid()) {
        return coverFile!!
    }
    //视频链接不为空
    if (file.fileUrl().isEmpty().not()) {
        val scheme = Uri.parse(file.fileUrl()).scheme
        val isLocalFileUrl = isNetworkScheme(scheme).not()

        //TODO: 只加载本地文件的视频封面
        //Coil加载网络视频封面的策略是将整个文件缓存至本地，再通过MediaMetadataRetriever获取视频帧
        //虽然在封面获取完成后会删除，但是这个过程会消耗大量流量、时间和存储空间
        //并且如果加载过程中退出应用，Coil并不会删除缓存文件，因此不使用其加载网络视频封面

        //考虑的方案：ijk的ffmpeg.so扩展命令行支持，参照Coil的HttpUriFetcher实现
        //只加载视频前后大概5M内容，再通过ffmpeg获取视频帧，简单测试可行
        //1.三次网络请求，1获取视频长度，2缓存前5M，3缓存后5M，使用RandomAccessFile写入文件对应位置
        //2.缓存后5M主要是MP4的moov box基本都在尾部，所以其它格式可以考虑不获取
        //3.5M内的视频帧位置不好确定，从（5M/视频长度*视频时长）中取？
        if (isLocalFileUrl) {
            source = file.fileUrl()
        }
    }
    //使用默认占位图
    return source ?: R.drawable.ic_dandanplay
}