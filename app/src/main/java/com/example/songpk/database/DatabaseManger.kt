package com.example.songpk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.songpk.database.daos.FavSongDao
import com.example.songpk.database.entities.FavSongs

@Database(entities = [FavSongs::class], version = 1)
abstract class DatabaseManger : RoomDatabase() {
    abstract fun favSongDao(): FavSongDao

    companion object {
        var INSTANCE: DatabaseManger? = null

        fun getInstance(context: Context): DatabaseManger {
            if (INSTANCE == null) {
                INSTANCE = Room
                    .databaseBuilder(context, DatabaseManger::class.java, "FAV_SONG_DB")
                    .build()
            }
            return INSTANCE as DatabaseManger
        }
    }
}