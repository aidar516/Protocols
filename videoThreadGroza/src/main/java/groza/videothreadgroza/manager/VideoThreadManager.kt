package groza.videothreadgroza.manager

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.ui.PlayerView
import groza.videothreadgroza.internet.AndroidConnectivityObserver
import groza.videothreadgroza.internet.ConnectivityObserver
import groza.videothreadgroza.model.StreamStatus
import groza.videothreadgroza.model.VideoThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.videolan.libvlc.util.VLCVideoLayout

class VideoThreadManager(
    context: Context,
    lifecycle: Lifecycle,
    private val connectivityObserver: ConnectivityObserver
) {
    private val rtmpManager = RtmpManager(context)
    private val rtspManager = RtspManager(context)
    private val udpManager = UdpManager(context)
    private val multicastManager = MulticastManager(context)

    private var currentManager: StreamManager? = null
    private var currentJob: Job? = null
    private var lastVideoThread: VideoThread? = null

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _streamStatus = MutableStateFlow<StreamStatus>(StreamStatus.Stopped)
    val streamStatus: StateFlow<StreamStatus> = _streamStatus

    init {
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> pause()
                    Lifecycle.Event.ON_RESUME -> resume()
                    Lifecycle.Event.ON_DESTROY -> destroy()
                    else -> {}
                }
            }
        })

        scope.launch {
            connectivityObserver.isConnected.collect { connected ->
                if (!connected) {
                    _streamStatus.value = StreamStatus.Reconnecting
                    stopStreamOnly()
                } else {
                    if (_streamStatus.value == StreamStatus.Reconnecting && lastVideoThread != null) {
                        delay(1000)
                        play(lastVideoThread!!)
                    }
                }
            }
        }
    }

    fun pause() {
        currentManager?.let { manager ->
            if (manager.isPlaying()) {
                Log.d("VideoThreadManager", "pause()")
                manager.pause()
            }
        }
    }

    fun resume() {
        lastVideoThread?.let { videoThread ->
            Log.d("VideoThreadManager", "resume() - videoThread: $videoThread")

            if (videoThread is VideoThread.RTSP) {
                Log.d("VideoThreadManager", "RTSP resume requires restart")
                stopStreamOnly()
                play(videoThread)
            } else {
                currentManager?.let { manager ->
                    if (!manager.isPlaying()) {
                        Log.d("VideoThreadManager", "Resuming non-RTSP stream")
                        manager.resume()
                    }
                }
            }
        }
    }

    fun play(videoThread: VideoThread) {
        lastVideoThread = videoThread
        currentJob?.cancel()

        currentJob = scope.launch {
            try {
                currentManager = when (videoThread) {
                    is VideoThread.RTMP -> rtmpManager
                    is VideoThread.RTSP -> rtspManager
                    is VideoThread.UDP -> udpManager
                    is VideoThread.UDPMulticast -> multicastManager
                }

                currentManager?.start(videoThread) { status ->
                    Log.d("VideoThreadManager", "Received status: $status")
                    _streamStatus.value = status
                }
            } catch (e: Exception) {
                Log.e("VideoThreadManager", "Failed to start stream", e)
                _streamStatus.value = StreamStatus.Error("Failed to start stream")
            }
        }
    }

    private fun stopStreamOnly() {
        Log.d("VideoThreadManager", "stopStreamOnly()")
        currentJob?.cancel()
        currentManager?.stopOnlyStream()
        currentManager = null
    }

    fun destroy() {
        Log.d("VideoThreadManager", "destroy()")
        stopStreamOnly()
    }

    fun attachView(videoThread: VideoThread, view: Any) {
        when (videoThread) {
            is VideoThread.RTSP -> rtspManager.attachView(view as VLCVideoLayout)
            is VideoThread.RTMP -> rtmpManager.attachView(view as PlayerView)
            is VideoThread.UDP -> udpManager.attachView(view as PlayerView)
            is VideoThread.UDPMulticast -> multicastManager.attachView(view as PlayerView)
        }
    }
}