package com.xyoye.stream_component.utils.remote

import com.xyoye.common_component.utils.FileComparator
import com.xyoye.data_component.data.remote.RemoteVideoData

/**
 * Created by xyoye on 2021/7/9.
 */

object RemoteFileHelper {
    private const val TAG_INVALID = "*"
    private const val TAG_FILE = "#"

    private const val separator = "/"

    /**
     * 将PC数据转换为树行形数据
     */
    fun convertTreeData(videoData: List<RemoteVideoData>): MutableList<RemoteVideoData> {
        return convertTreeData(
            videoData.map {
                //转换为文件路径格式
                it.absolutePath = formatPath(it.Path)
                it
            },
            separator
        )
    }

    /**
     * 将PC数据转换为树形数据
     */
    private fun convertTreeData(
        videoData: List<RemoteVideoData>,
        folderName: String
    ): MutableList<RemoteVideoData> {
        val treeList = mutableListOf<RemoteVideoData>()

        videoData.groupBy {
            //按文件夹分组
            if (it.absolutePath.isEmpty())
                return@groupBy TAG_INVALID
            if (it.absolutePath.startsWith(folderName).not())
                return@groupBy TAG_INVALID

            //剔除所有父目录
            val path = it.absolutePath.substring(folderName.length)
            //提取当前目录作为Key
            val separatorIndex = path.indexOf(separator)

            return@groupBy if (separatorIndex > -1) {
                it.absolutePath.substring(folderName.length, folderName.length + separatorIndex)
            } else {
                TAG_FILE
            }
        }.entries.forEach {
            if (it.key == TAG_FILE) {
                //文件直接加入列表
                treeList.addAll(it.value)
            } else if (it.key != TAG_INVALID) {
                val absolutePath = folderName + it.key + separator
                val child = convertTreeData(it.value, absolutePath)
                val displayName = child.run {
                    var firstAnimeTitle: String? = null
                    var hasSecond = false
                    for (i in 0 until child.size) {
                        val item = get(i)
                        if (item.isFolder.not() && item.AnimeTitle.isNotEmpty()) {
                            when (firstAnimeTitle) {
                                null -> firstAnimeTitle = item.AnimeTitle
                                else -> {
                                    if (item.AnimeTitle != firstAnimeTitle) {
                                        hasSecond = true
                                        break
                                    }
                                }
                            }
                        }
                    }
                    if (firstAnimeTitle != null && !hasSecond) {
                        "$firstAnimeTitle ( ${it.key} )"
                    } else {
                        for (item in child) {
                            if (!item.isFolder && item.AnimeTitle.isNotEmpty()) {
                                item.displayName = if (item.EpisodeTitle.isNotEmpty()) {
                                    "${item.AnimeTitle} - ${item.EpisodeTitle}"
                                } else {
                                    item.getEpisodeName()
                                }
                            }
                        }
                        it.key
                    }
                }
                val videoBean = RemoteVideoData(
                    isFolder = true,
                    Name = it.key,
                    absolutePath = absolutePath,
                    //目录则递归获取子文件及子目录
                    childData = child
                )
                videoBean.displayName = displayName
                treeList.add(videoBean)
            }
        }

        treeList.sortWith(FileComparator(
            value = {
                it.Name
            },
            isDirectory = {
                it.isFolder
            }
        ))

        return treeList
    }

    /**
     * 将PC文件路径转换为普通路径格式
     */
    private fun formatPath(path: String): String {
        var pcPath = ""


        val symbolIndex = path.indexOf(":\\")
        if (symbolIndex > 0)
            pcPath = path.substring(symbolIndex + 2)

        val absolutePath = StringBuilder()
        pcPath.split("\\").forEach {
            absolutePath.append(it).append(separator)
        }

        if (absolutePath.isEmpty())
            return ""

        return separator + absolutePath.substring(0, absolutePath.length - 1)
    }
}