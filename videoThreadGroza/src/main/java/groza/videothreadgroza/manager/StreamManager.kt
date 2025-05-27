package groza.videothreadgroza.manager

import groza.videothreadgroza.model.StreamStatus
import groza.videothreadgroza.model.VideoThread

internal interface StreamManager {
    fun start(videoThread: VideoThread, onStatusChanged: (StreamStatus) -> Unit)
    fun stopOnlyStream()
    fun destroy()
    fun pause()
    fun resume()
    fun isPlaying(): Boolean
}