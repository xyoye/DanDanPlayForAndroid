package com.xyoye.local_component.utils

import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.utils.SubtitleHashUtils
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.data_component.data.SubtitleShooterData
import com.xyoye.data_component.data.SubtitleSourceBean
import com.xyoye.data_component.data.SubtitleThunderData

object SubtitleMatchHelper {

    suspend fun matchSubtitle(videoPath: String): MutableList<SubtitleSourceBean> {
        val sourceList = mutableListOf<SubtitleSourceBean>()
        sourceList.addAll(matchThunderSubtitle(videoPath))
        sourceList.addAll(matchShooterSubtitle(videoPath))
        return sourceList
    }

    private suspend fun matchThunderSubtitle(videoPath: String): MutableList<SubtitleSourceBean> {
        val subtitleList = mutableListOf<SubtitleSourceBean>()

        val videoHash = SubtitleHashUtils.getThunderHash(videoPath) ?: return subtitleList

        val thunderUrl = "http://sub.xmp.sandai.net:8000/subxl/$videoHash.json"

        var subtitleData: SubtitleThunderData? = null
        try {
            subtitleData = Retrofit.extService.matchThunderSubtitle(thunderUrl)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (subtitleData?.sublist?.size ?: 0 > 0) {
            for (thunderData in subtitleData!!.sublist!!) {
                val sourceUrl = thunderData.surl ?: continue
                val sourceBean = SubtitleSourceBean(
                    isMatch = true,
                    name = thunderData.sname,
                    matchUrl = sourceUrl,
                    source = "迅雷"
                )
                subtitleList.add(sourceBean)
            }
        }
        return subtitleList
    }

    private suspend fun matchShooterSubtitle(videoPath: String): MutableList<SubtitleSourceBean> {
        val subtitleList = mutableListOf<SubtitleSourceBean>()

        val videoHash = SubtitleHashUtils.getShooterHash(videoPath) ?: return subtitleList

        val shooterParams = HashMap<String, String>()
        shooterParams["filehash"] = videoHash
        shooterParams["pathinfo"] = getFileName(videoPath)
        shooterParams["format"] = "json"
        shooterParams["lang"] = "Chn"

        val shooterUrl = "https://www.shooter.cn/api/subapi.php"

        var shooterSubtitleList: MutableList<SubtitleShooterData>? = null
        try {
            shooterSubtitleList = Retrofit.extService
                .matchShooterSubtitle(shooterUrl, shooterParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        if (shooterSubtitleList != null) {
            for (subtitleData in shooterSubtitleList) {
                val subtitleFiles = subtitleData.Files ?: continue
                for (shooterData in subtitleFiles) {
                    val downloadUrl = shooterData.Link ?: continue

                    val extension = shooterData.Ext ?: ".ass"
                    val shooterName = getFileNameNoExtension(videoPath) + "." + extension

                    val sourceBean = SubtitleSourceBean(
                        isMatch = true,
                        name = shooterName,
                        matchUrl = downloadUrl,
                        source = "射手网"
                    )
                    subtitleList.add(sourceBean)
                }
            }
        }
        return subtitleList
    }
}