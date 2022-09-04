package com.example.songpk.utils

import com.example.songpk.database.pojo.SongsModel
import com.google.android.exoplayer2.Player

data class PlayerUriModels(var songsModel: SongsModel? = null) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerUriModels

        if (songsModel != other.songsModel) return false

        return true
    }

    override fun hashCode(): Int {
        return songsModel.hashCode()
    }
}

data class PlayerUIModels(var songsModel: SongsModel? = null, var playerState: Int = Player.STATE_ENDED) {

}