package com.example.songpk.activitys

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.example.songpk.R
import com.example.songpk.adapters.SongAdapters
import com.example.songpk.service.PlayerService
import com.example.songpk.utils.FilesManger
import com.example.songpk.utils.PlayerUriModels
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val filesManger = lazy {
        FilesManger.getInstance()
    }

    private lateinit var adapter: SongAdapters

    private val resultCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Storage Permission is not granted", Toast.LENGTH_SHORT).show()
            }
            fetchData()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeData()
        initializeList()
        intilizeUI()
    }

    private fun initializeList() {
        val recyclerView = findViewById<RecyclerView>(R.id.rv)
        adapter = SongAdapters(this)
        recyclerView.adapter = adapter

        filesManger.value.mlist.observe(this) {
            adapter.updateList(it)
        }

        PlayerService.start(this)
    }

    private fun intilizeUI(){
        val playerLayout = findViewById<LinearLayout>(R.id.player_layout)
        val song_tv = findViewById<TextView>(R.id.song_tv)
        val song_action = findViewById<ImageView>(R.id.song_action)

        filesManger.value.mPlayerUiEvent.observe(this) {
            playerLayout.visibility = View.VISIBLE
            it.songsModel?.let { model->
                song_tv.text = model.songsName
            }
            song_action.setImageResource(if(it.playerState == 0){
                song_tv.isSelected = true
                android.R.drawable.ic_media_pause
            }else{
                song_tv.isSelected = false
                android.R.drawable.ic_media_play
            })
        }
        song_action?.setOnClickListener {
            filesManger.value.mPlayerEvent.postValue(PlayerUriModels())
        }
    }

    private fun initializeData() {
        if (filesManger.value.checkPermission(this)) {
            fetchData()
        } else {
            resultCallback.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun fetchData() {
        GlobalScope.launch {
            filesManger.value.fetchAudioFiles(this@MainActivity)
        }
    }
}