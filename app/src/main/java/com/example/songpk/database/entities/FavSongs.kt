package com.example.songpk.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favsongs")
data class FavSongs(
    @PrimaryKey(autoGenerate = true) var uid: Int? = null,
    @ColumnInfo(name = "song_id") var songid: Int,
    @ColumnInfo(name = "fav") var isFavSongs: Boolean
)
