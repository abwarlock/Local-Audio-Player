package com.example.songpk.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.songpk.R
import com.example.songpk.database.DatabaseManger
import com.example.songpk.database.entities.FavSongs
import com.example.songpk.database.pojo.SongsModel
import com.example.songpk.utils.FilesManger
import com.example.songpk.utils.PlayerUriModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class SongAdapters(private val context: Context) : RecyclerView.Adapter<SongAdapters.SongViewHolder>() {

    private var songsList = mutableListOf<SongsModel>()

    val favSongDao = lazy {
        DatabaseManger.getInstance(context).favSongDao()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(context).inflate(R.layout.songs_item_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bindView(songsList[position])
    }

    override fun getItemCount() = songsList.size

    fun updateList(list: List<SongsModel>) {
        songsList.clear()
        songsList.addAll(list)
        notifyDataSetChanged()
    }


    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtView = itemView.findViewById(R.id.text_name) as TextView?
        var ivFav = itemView.findViewById(R.id.iv_fav) as ImageView?

        fun bindView(songsModel: SongsModel) {
            txtView?.text = songsModel.songsName

            GlobalScope.launch {
                val fav = favSongDao.value.getIsFav(songsModel.id) ?: false
                ivFav?.setImageResource(
                    if (fav) {
                        R.drawable.outline_favorite_24
                    } else {
                        R.drawable.outline_favorite_border_24
                    }
                )
            }

            ivFav?.setOnClickListener {
                val model = songsList[adapterPosition]
                GlobalScope.launch {
                    var favSong = favSongDao.value.getFaveSong(songsModel.id)
                    if (favSong == null) {
                        favSong = FavSongs(songid = model.id, isFavSongs = true)
                    } else {
                        favSong.isFavSongs = !favSong.isFavSongs
                    }
                    favSongDao.value.markFave(favSong)
                    updatePosition(adapterPosition)
                }
            }

            txtView?.setOnClickListener {
                val model = songsList[adapterPosition]
                FilesManger.getInstance().mPlayerEvent.postValue(PlayerUriModels(model))
            }
        }

        private fun updatePosition(adapterPosition: Int) {
            GlobalScope.launch(Dispatchers.Main) {
                notifyItemChanged(adapterPosition)
            }
        }
    }
}