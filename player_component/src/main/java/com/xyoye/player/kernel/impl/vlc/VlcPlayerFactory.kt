package com.xyoye.player.kernel.impl.vlc

import android.content.Context
import com.xyoye.player.kernel.facoty.PlayerFactory
import com.xyoye.player.kernel.inter.AbstractVideoPlayer

/**
 * Created by xyoye on 2021/4/12.
 */

class VlcPlayerFactory : PlayerFactory() {

    override fun createPlayer(context: Context): AbstractVideoPlayer {
        return VlcVideoPlayer(context)
    }
}