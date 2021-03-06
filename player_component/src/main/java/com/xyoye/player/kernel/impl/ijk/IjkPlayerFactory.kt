package com.xyoye.player.kernel.impl.ijk

import android.content.Context
import com.xyoye.player.kernel.facoty.PlayerFactory
import com.xyoye.player.kernel.inter.AbstractVideoPlayer

/**
 * Created by xyoye on 2020/10/29.
 */

class IjkPlayerFactory : PlayerFactory() {

    override fun createPlayer(context: Context): AbstractVideoPlayer {
        return IjkVideoPlayer(context)
    }
}