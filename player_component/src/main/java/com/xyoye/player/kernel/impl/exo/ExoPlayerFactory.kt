package com.xyoye.player.kernel.impl.exo

import android.content.Context
import com.xyoye.player.kernel.facoty.PlayerFactory
import com.xyoye.player.kernel.inter.AbstractVideoPlayer

/**
 * Created by xyoye on 2020/11/1.
 */

class ExoPlayerFactory : PlayerFactory() {

    override fun createPlayer(context: Context): AbstractVideoPlayer {
        return ExoVideoPlayer(context)
    }

}