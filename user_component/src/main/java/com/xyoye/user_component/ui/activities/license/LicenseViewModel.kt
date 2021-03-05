package com.xyoye.user_component.ui.activities.license

import android.content.res.AssetManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.getFileNameNoExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class LicenseViewModel : BaseViewModel() {

    val licenseLiveData = MutableLiveData<MutableList<Pair<String, String>>>()

    fun getLicense() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                showLoading()

                val licenses = mutableListOf<Pair<String, String>>()
                val assets = BaseApplication.getAppContext().assets
                assets.list("license")?.forEach {
                    getLicense(assets, it)?.let { license ->
                        licenses.add(license)
                    }
                }

                hideLoading()
                licenseLiveData.postValue(licenses)
            }
        }
    }

    private fun getLicense(assets: AssetManager, fileName: String): Pair<String, String>? {
        var pair: Pair<String, String>? = null

        var inputStream: InputStream? = null
        var bufferedReader: BufferedReader? = null

        try {
            var projectName = getFileNameNoExtension(fileName)
            inputStream = assets.open("license/$fileName")
            bufferedReader = BufferedReader(InputStreamReader(inputStream))

            val licenseBuilder = StringBuilder()

            val textList = bufferedReader.readLines()
            for ((line, text) in textList.withIndex()) {
                //第一行是网址
                if (line == 0 && text.startsWith("#")) {
                    projectName += "( ${text.substring(1, text.length)} )"
                } else {
                    licenseBuilder.append(text.trim())
                    if (line < textList.size - 1) {
                        licenseBuilder.append("\n")
                    }
                }
            }

            pair = Pair(projectName, licenseBuilder.toString())
        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            IOUtils.closeIO(bufferedReader)
            IOUtils.closeIO(inputStream)
        }

        return pair
    }
}