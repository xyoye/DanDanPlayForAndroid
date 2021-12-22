package setup.utils

import Versions
import com.android.build.gradle.api.BaseVariantOutput
import java.text.SimpleDateFormat
import java.util.*

object OutputHelper {
    private val format = SimpleDateFormat("MMddHHmm", Locale.CHINA)

    fun outputFileName(variant: BaseVariantOutput): String {
        val time = format.format(Date())
        return "dandanplay_v${Versions.versionName}_${variant.name}_$time.apk"
    }
}