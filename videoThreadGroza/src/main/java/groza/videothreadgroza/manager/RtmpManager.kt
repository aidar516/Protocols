package groza.videothreadgroza.manager

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.rtmp.RtmpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import groza.videothreadgroza.model.VideoThread
import androidx.media3.ui.PlayerView
import groza.videothreadgroza.model.StreamStatus

internal class RtmpManager(private val context: Context): StreamManager {
    private val TAG = "RtmpManager"

    private var exoPlayer: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var currentVideoThread: VideoThread.RTMP? = null
    private var onStatusChangedCallback: ((StreamStatus) -> Unit)? = null

    fun attachView(view: PlayerView) {
        Log.d(TAG, "attachView called")
        playerView = view
    }

    @OptIn(UnstableApi::class)
    override fun start(videoThread: VideoThread, onStatusChanged: (StreamStatus) -> Unit) {
        onStatusChangedCallback = onStatusChanged

        if(videoThread !is VideoThread.RTMP) {
            throw Exception("This not rtmp thread video")
        }

        currentVideoThread = videoThread

        exoPlayer = ExoPlayer.Builder(context)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        /* minBufferMs = */ 2000,
                        /* maxBufferMs = */ 5000,
                        /* bufferForPlaybackMs = */ 1000,
                        /* bufferForPlaybackAfterRebufferMs = */ 2000
                    )
                    .build()
            )
            .build().apply {
                val dataSourceFactory = RtmpDataSource.Factory()
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(videoThread.url))

                setMediaSource(mediaSource)
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
                playWhenReady = true

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                Log.d(TAG, "Playback ready")
                                onStatusChanged(StreamStatus.Playing)
                            }
                            Player.STATE_ENDED -> {
                                Log.d(TAG, "Playback ended")
                                onStatusChanged(StreamStatus.Stopped)
                            }
                            Player.STATE_IDLE -> {
                                Log.d(TAG, "Idle state")
                                onStatusChanged(StreamStatus.Stopped)
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d(TAG, "Is playing: $isPlaying")
                        if (isPlaying) {
                            onStatusChanged(StreamStatus.Playing)
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e(TAG, "Playback error: ${error.errorCodeName}", error)
                        onStatusChanged(StreamStatus.Error("Ошибка воспроизведения: ${error.message}"))
                    }
                })
            }

        playerView?.player = exoPlayer
    }

    override fun stopOnlyStream() {
        exoPlayer?.stop()
    }

    override fun destroy() {
        exoPlayer?.release()
        exoPlayer = null
        playerView?.player = null
        currentVideoThread = null
    }

    override fun pause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.playWhenReady = false
                player.pause()
            } else {
                Log.d(TAG, "Pause skipped, player not playing")
            }
        } ?: Log.d(TAG, "Pause skipped, player is null")
    }

    override fun resume() {
        if (exoPlayer == null) {
            currentVideoThread?.let { start(it, onStatusChanged = onStatusChangedCallback ?: {}) }
            return
        }

        val player = exoPlayer!!

        if (player.isPlaying) return

        when (player.playbackState) {
            Player.STATE_ENDED, Player.STATE_IDLE -> {
                currentVideoThread?.let { start(it, onStatusChanged = onStatusChangedCallback ?: {}) }
            }
            Player.STATE_READY, Player.STATE_BUFFERING -> {
                player.playWhenReady = true
            }
            else -> {
                player.playWhenReady = true
            }
        }
    }

    override fun isPlaying(): Boolean {
        val playing = exoPlayer?.isPlaying ?: false
        return playing
    }
}
