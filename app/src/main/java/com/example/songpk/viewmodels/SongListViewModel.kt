package com.example.songpk.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.songpk.database.pojo.SongsModel

class SongListViewModel : ViewModel() {
    var list = MutableLiveData<List<SongsModel>>()
}