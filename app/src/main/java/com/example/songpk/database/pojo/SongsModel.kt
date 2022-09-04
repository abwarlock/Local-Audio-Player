package com.example.songpk.database.pojo

data class SongsModel(
    val id: Int,
    val songsName: String,
    val filePath: String,
    val artists: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SongsModel

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}
