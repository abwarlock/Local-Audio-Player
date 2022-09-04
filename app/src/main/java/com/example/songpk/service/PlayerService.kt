package com.example.songpk.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import androidx.annotation.MainThread
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.LifecycleService
import com.example.songpk.R
import com.example.songpk.activitys.MainActivity
import com.example.songpk.utils.FilesManger
import com.example.songpk.utils.PlayerUIModels
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSource
import timber.log.Timber
import java.io.File


class PlayerService : LifecycleService() {

    private lateinit var exoPlayer: ExoPlayer

    private var playerNotificationManager: PlayerNotificationManager? = null
    private val PLAYBACK_CHANNEL_ID = "playback_channel"
    private val PLAYBACK_NOTIFICATION_ID = 11

    companion object {
        @MainThread
        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, PlayerService::class.java)
            )
        }

    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                PLAYBACK_CHANNEL_ID,
                PLAYBACK_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            val notification: Notification = Notification.Builder(this, PLAYBACK_CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(PLAYBACK_NOTIFICATION_ID, notification)
        }
        FilesManger.getInstance().mPlayerEvent.observe(this) {
            if(it.songsModel == null){
                exoPlayer.playWhenReady = !exoPlayer.isPlaying
            }else{
                it.songsModel?.let {model->
                    if (!FilesManger.getInstance().songList.contains(it.songsModel)) {
                        File(model.filePath).let { file->
                            if(file.exists()){
                                FilesManger.getInstance().songList.add(model)
                                play(Uri.fromFile(File(model.filePath)))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startPlayer()
        return START_STICKY
    }

    private fun startPlayer() {
        exoPlayer = ExoPlayer.Builder(this, DefaultMediaSourceFactory(this))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setFlags(C.FLAG_AUDIBILITY_ENFORCED)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build(), true
            )
            .build()

        playerNotificationManager =
            PlayerNotificationManager.Builder(this, PLAYBACK_NOTIFICATION_ID, PLAYBACK_CHANNEL_ID)
                .setMediaDescriptionAdapter(object :
                    PlayerNotificationManager.MediaDescriptionAdapter {
                    override fun getCurrentContentTitle(player: Player): CharSequence {
                        val songsModel =
                            FilesManger.getInstance().songList[player.currentMediaItemIndex]
                        return songsModel.songsName;
                    }

                    override fun createCurrentContentIntent(player: Player): PendingIntent? {
                        return PendingIntent.getActivity(
                            applicationContext,
                            0,
                            Intent(applicationContext, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )

                    }

                    override fun getCurrentContentText(player: Player): CharSequence? {
                        val songsModel =
                            FilesManger.getInstance().songList[player.currentMediaItemIndex]
                        return songsModel.artists
                    }

                    override fun getCurrentLargeIcon(
                        player: Player,
                        callback: PlayerNotificationManager.BitmapCallback
                    ): Bitmap? {
                        return ContextCompat.getDrawable(this@PlayerService, R.mipmap.ic_launcher)
                            ?.let {
                                val drawable = DrawableCompat.wrap(it).mutate()
                                val bitmap = Bitmap.createBitmap(
                                    drawable.intrinsicWidth,
                                    drawable.intrinsicHeight,
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = Canvas(bitmap)
                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                drawable.draw(canvas)
                                bitmap
                            }
                    }

                })
                .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                    override fun onNotificationCancelled(
                        notificationId: Int,
                        dismissedByUser: Boolean
                    ) {
                        stopSelf()
                    }

                    override fun onNotificationPosted(
                        notificationId: Int,
                        notification: Notification,
                        ongoing: Boolean
                    ) {
                        if (ongoing) {
                            startForeground(notificationId, notification)
                        } else {
                            stopForeground(false)
                        }
                    }
                })
                .build()


        playerNotificationManager?.setVisibility(VISIBILITY_PUBLIC)
        playerNotificationManager?.setUseNextAction(true)
        playerNotificationManager?.setUsePreviousAction(true)
        playerNotificationManager?.setUseFastForwardAction(false)
        playerNotificationManager?.setUseRewindAction(false)
        playerNotificationManager?.setUsePlayPauseActions(true)

        playerNotificationManager?.setPlayer(exoPlayer)

        exoPlayer.addListener(PlayerEventListener())
        exoPlayer.playWhenReady = true
    }

    inner class PlayerEventListener : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            val instance = FilesManger.getInstance()
            if (playbackState == Player.STATE_READY) {
                if (exoPlayer.playWhenReady) {
                    instance.mPlayerUiEvent.postValue(
                        PlayerUIModels(
                            instance.getItem(exoPlayer.currentMediaItemIndex),
                            if (exoPlayer.isPlaying) 0 else -1
                        )
                    )
                    Timber.d("STATE_READY")
                } else {
                    Timber.d("NOT STATE_READY")
                }
            } else if (playbackState == Player.STATE_ENDED) {
                Timber.d("STATE_ENDED")
            }
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            if (playbackState == Player.STATE_READY) {
                val instance = FilesManger.getInstance()
                instance.mPlayerUiEvent.postValue(
                    PlayerUIModels(
                        instance.getItem(exoPlayer.currentMediaItemIndex),
                        if (exoPlayer.isPlaying) 0 else -1
                    )
                )
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Timber.e(error)
        }
    }


    private fun play(uri: Uri?) {
        uri?.let {
            val createMediaSource = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(this))
                .createMediaSource(MediaItem.fromUri(it))
            exoPlayer.addMediaSource(createMediaSource)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }

    }

    override fun onDestroy() {
        playerNotificationManager?.setPlayer(null)
        exoPlayer.release()

        super.onDestroy()
    }
}