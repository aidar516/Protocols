package groza.videothreadgroza.manager

import android.content.Context
import android.net.Uri
import android.util.Log
import groza.videothreadgroza.model.StreamStatus
import groza.videothreadgroza.model.VideoThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

internal class RtspManager(private val context: Context): StreamManager {
    private val TAG = "RtspManager"
    private val libVLC = LibVLC(context, arrayListOf(
        "--rtsp-tcp", "--network-caching=1000", "--demux=ts", "--drop-late-frames",
        "--skip-frames", "--rtsp-frame-buffer-size=500000", "--avcodec-codec=h264",
        "--file-caching=500", "--live-caching=1500", "--clock-jitter=0", "--verbose=2",
        "--adaptive-logic=rate", "--sout-mux-caching=2000"
    ))

    private val mediaPlayer = MediaPlayer(libVLC)

    private var vlcView: VLCVideoLayout? = null
    private var currentVideoThread: VideoThread.RTSP? = null
    private var isViewAttached = false

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        Log.d(TAG, "RtspManager initialized")
        mediaPlayer.setEventListener { event ->
            Log.d(TAG, "VLC Event: ${event.type}")
        }
    }

    fun attachView(vlcView: VLCVideoLayout) {
        Log.d(TAG, "attachView called")
        this.vlcView = vlcView
        if (!isViewAttached) {
            mediaPlayer.attachViews(vlcView, null, false, false)
            isViewAttached = true
            Log.d(TAG, "View attached to mediaPlayer")
        } else {
            Log.d(TAG, "View already attached")
        }
    }

    private fun reattachViewIfNeeded() {
        Log.d(TAG, "reattachViewIfNeeded called")
        if (!isViewAttached && vlcView != null) {
            mediaPlayer.attachViews(vlcView!!, null, false, false)
            isViewAttached = true
            Log.d(TAG, "Re-attached view")
        } else {
            Log.d(TAG, "No need to reattach view")
        }
    }

    override fun start(videoThread: VideoThread, onStatusChanged: (StreamStatus) -> Unit) {
        Log.d(TAG, "start called with thread: $videoThread")

        if (videoThread !is VideoThread.RTSP) {
            Log.e(TAG, "Invalid thread type")
            throw Exception("This is not an RTSP video thread")
        }

        currentVideoThread = videoThread

        val login = videoThread.login ?: ""
        val password = videoThread.password ?: ""
        val baseUrl = videoThread.url

        val uri = if (login.isNotEmpty() && password.isNotEmpty()) {
            val baseUri = Uri.parse(baseUrl)
            baseUri.buildUpon()
                .encodedAuthority("$login:$password@${baseUri.host}:${baseUri.port}")
                .build()
        } else {
            Uri.parse(baseUrl)
        }

        val media = Media(libVLC, uri).apply {
            setHWDecoderEnabled(true, false)
            addOption(":network-caching=150")
            if (login.isNotEmpty()) addOption(":rtsp-user=$login")
            if (password.isNotEmpty()) addOption(":rtsp-pwd=$password")
        }

        mediaPlayer.setEventListener { event ->
            when (event.type) {
                MediaPlayer.Event.Opening -> {
                    Log.i(TAG, "Opening stream")
                    onStatusChanged(StreamStatus.Connecting)
                }
                MediaPlayer.Event.Playing -> {
                    onStatusChanged(StreamStatus.Playing)
                }
                MediaPlayer.Event.EncounteredError -> {
                    onStatusChanged(StreamStatus.Error("Ошибка подключения к потоку"))
                    scope.launch {
                        delay(2000)
                        currentVideoThread?.let { start(it, onStatusChanged) }
                    }
                }
                MediaPlayer.Event.EndReached -> {
                    onStatusChanged(StreamStatus.Error("Передача потока завершена"))
                    scope.launch {
                        delay(1000)
                        currentVideoThread?.let { start(it, onStatusChanged) }
                    }
                }
                MediaPlayer.Event.Stopped -> {
                    onStatusChanged(StreamStatus.Stopped)
                }
                else -> {}
            }
        }

        mediaPlayer.media = media
        media.release()

        Log.d(TAG, "Media assigned and released")

        reattachViewIfNeeded()
        mediaPlayer.play()

        Log.d(TAG, "Playback started")
    }

    override fun stopOnlyStream() {
        Log.d(TAG, "stopOnlyStream called")
        mediaPlayer.stop()
        mediaPlayer.detachViews()
        isViewAttached = false
        Log.d(TAG, "Playback stopped and views detached")
    }

    override fun destroy() {
        Log.d(TAG, "destroy called")
        mediaPlayer.stop()
        mediaPlayer.detachViews()
        mediaPlayer.release()
        libVLC.release()
        Log.d(TAG, "Resources released")
    }

    override fun isPlaying(): Boolean {
        val playing = mediaPlayer.isPlaying
        Log.d(TAG, "isPlaying: $playing")
        return playing
    }

    override fun pause() {
        Log.d(TAG, "pause called - no-op for RTSP")
        // Для RTSP пауза не поддерживается
    }

    override fun resume() {
        Log.d(TAG, "resume called - no-op for RTSP")
        // Для RTSP возобновление воспроизведения через перезапуск потока вне этого класса
    }

}