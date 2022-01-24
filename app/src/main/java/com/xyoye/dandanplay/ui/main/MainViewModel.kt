package com.xyoye.dandanplay.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.UserConfig
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.database.migration.ManualMigration
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.UserInfoHelper
import com.xyoye.data_component.data.LoginData
import com.xyoye.data_component.entity.DanmuBlockEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.w3c.dom.Element
import java.io.InputStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Created by xyoye on 2020/7/27.
 */

class MainViewModel : BaseViewModel() {

    val reLoginLiveData = MutableLiveData<LoginData>()

    fun reLogin() {
        UserConfig.putUserLoggedIn(false)
        httpRequest<LoginData>(viewModelScope) {
            api {
                Retrofit.service.reLogin()
            }

            onSuccess {
                if (UserInfoHelper.login(it)) {
                    reLoginLiveData.postValue(it)
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
        if (currentTime - lastUpdateTime > 7 * 24 * 60 * 60) {

            httpRequest<MutableList<String>>(viewModelScope) {

                api {
                    val filterUrl = "https://api.acplay.net/config/filter.xml"
                    val responseBody = Retrofit.extService.downloadResource(filterUrl)

                    parseFilterData(responseBody.byteStream())
                }

                onSuccess {
                    AppConfig.putCloudBlockUpdateTime(currentTime)
                    saveFilterData(it)
                }

                onError {
                    it.printStackTrace()
                }

            }
        }
    }

    private fun parseFilterData(inputStream: InputStream): MutableList<String> {
        return runBlocking(Dispatchers.IO) {

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

            mutableListOf(
                simplified.toString(),
                traditional.toString()
            )
        }
    }

    private fun saveFilterData(filterData: MutableList<String>) {
        viewModelScope.launch {
            val blockEntities = filterData.map { keyword ->
                DanmuBlockEntity(
                    0,
                    keyword,
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
}