package com.xyoye.data_component.data.screeencast

import android.os.Parcelable
import com.xyoye.data_component.data.CommonJsonData
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class RemotePlayerStatus(
    val title: String? = null,
    val playing: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val bufferedPercent: Int = 0,
    val speed: Float = 1f,
    val volumePercent: Int = -1,
    val brightnessPercent: Int = -1,
    val mediaType: String? = null
) : Parcelable

@Parcelize
@Serializable
class RemoteControlResult(
    var status: RemotePlayerStatus? = null
) : CommonJsonData(), Parcelable {
    companion object {
        fun success(status: RemotePlayerStatus? = null): RemoteControlResult {
            return RemoteControlResult(status).apply {
                success = true
                errorCode = 0
                errorMessage = null
            }
        }

        fun failure(code: Int, message: String): RemoteControlResult {
            return RemoteControlResult().apply {
                success = false
                errorCode = code
                errorMessage = message
            }
        }
    }
}
