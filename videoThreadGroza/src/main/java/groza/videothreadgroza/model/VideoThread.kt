package groza.videothreadgroza.model

sealed class VideoThread(open val url: String) {
    data class RTMP(override val url: String, val password: String?): VideoThread(url = url)
    data class RTSP(override val url: String, val login: String?, val password: String?): VideoThread(url = url)
    data class UDP(override val url: String, val password: String?): VideoThread(url = url)
    data class UDPMulticast(override val url: String, val password: String?): VideoThread(url = url)
}