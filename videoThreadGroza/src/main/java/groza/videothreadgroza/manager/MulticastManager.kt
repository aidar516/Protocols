package groza.videothreadgroza.manager

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.ui.PlayerView
import groza.videothreadgroza.UdpDataSourceFactory
import groza.videothreadgroza.model.StreamStatus
import groza.videothreadgroza.model.VideoThread

internal class MulticastManager(private val context: Context): StreamManager {

    private val TAG = "MulticastManager"

    private var exoPlayer: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var currentVideoThread: VideoThread.UDPMulticast? = null
    private var onStatusChangedCallback: ((StreamStatus) -> Unit)? = null

    fun attachView(view: PlayerView) {
        playerView = view
    }

    @OptIn(UnstableApi::class)
    override fun start(videoThread: VideoThread, onStatusChanged: (StreamStatus) -> Unit) {
        onStatusChangedCallback = onStatusChanged

        if (videoThread !is VideoThread.UDPMulticast) {
            throw Exception("This is not a UDP multicast video thread")
        }

        currentVideoThread = videoThread

        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                /* minBufferMs = */ 1000,
                /* maxBufferMs = */ 2000,
                /* bufferForPlaybackMs = */ 500,
                /* bufferForPlaybackAfterRebufferMs = */ 1000
            )
            .build()

        exoPlayer = ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build().apply {
                val udpDataSourceFactory = UdpDataSourceFactory()
                val mediaSource = ProgressiveMediaSource.Factory(udpDataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(videoThread.url))

                setMediaSource(mediaSource)
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
            }
        }
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
        return exoPlayer?.isPlaying ?: false
    }
}