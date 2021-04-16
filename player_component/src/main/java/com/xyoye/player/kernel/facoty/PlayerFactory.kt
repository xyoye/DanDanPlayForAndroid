package com.xyoye.player.kernel.facoty

import android.content.Context
import com.xyoye.data_component.enums.PlayerType
import com.xyoye.player.kernel.impl.exo.ExoPlayerFactory
import com.xyoye.player.kernel.impl.ijk.IjkPlayerFactory
import com.xyoye.player.kernel.impl.vlc.VlcPlayerFactory
import com.xyoye.player.kernel.inter.AbstractVideoPlayer

/**
 * Created by xyoye on 2020/10/29.
 */

abstract class PlayerFactory {

    companion object {
        fun getFactory(playerType: PlayerType): PlayerFactory {
            return when (playerType) {
                PlayerType.TYPE_EXO_PLAYER -> ExoPlayerFactory()
                PlayerType.TYPE_IJK_PLAYER -> IjkPlayerFactory()
                PlayerType.TYPE_VLC_PLAYER -> VlcPlayerFactory()
                else -> IjkPlayerFactory()
            }
        }
    }

    abstract fun createPlayer(context: Context): AbstractVideoPlayer
}