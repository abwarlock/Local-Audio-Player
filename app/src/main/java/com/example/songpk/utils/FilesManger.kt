package com.example.songpk.utils

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.songpk.database.pojo.SongsModel
import timber.log.Timber
import kotlin.coroutines.suspendCoroutine


class FilesManger private constructor() {

    companion object {
        var instanceVar: FilesManger? = null

        @JvmName("getInstance")
        fun getInstance(): FilesManger {
            if (instanceVar == null) {
                synchronized(this) {
                    instanceVar = FilesManger()
                }
            }
            return instanceVar as FilesManger
        }
    }

    var mlist = MutableLiveData<List<SongsModel>>()

    var mPlayerEvent = MutableLiveData<PlayerUriModels>()

    var songList = mutableListOf<SongsModel>()

    var mPlayerUiEvent = MutableLiveData<PlayerUIModels>()


    fun getItem(pos: Int) = songList[pos]

    private val proj = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME)

    @SuppressLint("Range")
    suspend fun fetchAudioFiles(context: Context) {
        suspendCoroutine<Void> {
            val list = mutableListOf<SongsModel>()
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null
            )?.let {
                try {
                    if (it.moveToFirst()) {
                        do {
                            val isMusic: Int =
                                it.getInt(it.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC))
                            if (isMusic != 0) {
                                val path =
                                    it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                                val index =
                                    it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                                val index1 = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                                val artist =
                                    it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                                list.add(
                                    SongsModel(
                                        it.getInt(index1),
                                        it.getString(index),
                                        path,
                                        artist
                                    )
                                )
                            }
                        } while (it.moveToNext())
                    }
                    it.close()
                } catch (ex: Exception) {
                    Timber.e(ex)
                }
                mlist.postValue(list)
            }
        }
    }

    fun checkPermission(context: Context): Boolean {

        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            context,
            READ_EXTERNAL_STORAGE
        )
    }

}