package com.example.songpk.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.songpk.database.entities.FavSongs

@Dao
interface FavSongDao {

    @Query("SELECT * FROM favsongs")
    fun getAll(): List<FavSongs>

    @Query("SELECT fav FROM favsongs where song_id = :id")
    suspend fun getIsFav(id: Int): Boolean?

    @Query("SELECT * FROM favsongs where song_id = :id")
    suspend fun getFaveSong(id: Int): FavSongs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markFave(model: FavSongs)

}