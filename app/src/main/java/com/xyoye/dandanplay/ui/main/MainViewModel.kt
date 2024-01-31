package com.xyoye.dandanplay.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.UserConfig
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.database.migration.ManualMigration
import com.xyoye.common_component.network.repository.OtherRepository
import com.xyoye.common_component.network.repository.UserRepository
import com.xyoye.common_component.utils.UserInfoHelper
import com.xyoye.data_component.data.LoginData
import com.xyoye.data_component.entity.DanmuBlockEntity
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import java.io.InputStream
import java.util.Date
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Created by xyoye on 2020/7/27.
 */

class MainViewModel : BaseViewModel() {

    val reLoginLiveData = MutableLiveData<LoginData>()

    fun reLogin() {
        viewModelScope.launch {
            val result = UserRepository.refreshToken()

            if (result.isSuccess) {
                UserConfig.putUserLoggedIn(false)
                if (UserInfoHelper.login(result.getOrThrow())) {
                    reLoginLiveData.postValue(result.getOrThrow())
                }
            }
        }
    }

    fun initDatabase() {
        viewModelScope.launch {
            ManualMigration.migrate()
        }
    }

    fun initCloudBlockData() {
        val lastUpdateTime = AppConfig.getCloudBlockUpdateTime()
        val currentTime = System.currentTimeMillis()

        //7天更新一次
        if (currentTime - lastUpdateTime < 7 * 24 * 60 * 60 * 1000) {
            return
        }

        viewModelScope.launch {
            val result = OtherRepository.getCloudFilters()

            result.getOrNull()?.byteStream()?.use {
                val filterData = parseFilterData(it)
                saveFilterData(filterData)
                AppConfig.putCloudBlockUpdateTime(currentTime)
            }

        }
    }

    private fun parseFilterData(inputStream: InputStream): List<String> {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(inputStream)
            val nodeList = document.getElementsByTagName("FilterItem")

            val traditional = StringBuilder()
            val simplified = StringBuilder()

            for (index in 0..nodeList.length) {
                val element = nodeList.item(index) as Element?
                val tagName = element?.getAttribute("Name")
                if (tagName?.endsWith("简体") == true) {
                    val content = element.textContent
                    if (content?.isNotEmpty() == true) {
                        simplified.append(content)
                    }
                } else if (tagName?.endsWith("繁体") == true) {
                    val content = element.textContent
                    if (content?.isNotEmpty() == true) {
                        traditional.append(content)
                    }
                }
            }
            listOf(simplified.toString(), traditional.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun saveFilterData(filterData: List<String>) {
        val blockEntities = filterData.map {
            DanmuBlockEntity(
                0,
                it,
                true,
                Date(),
                true
            )
        }
        DatabaseManager.instance.getDanmuBlockDao().deleteByType(true)
        DatabaseManager.instance.getDanmuBlockDao().insert(
            *blockEntities.toTypedArray()
        )
    }
}