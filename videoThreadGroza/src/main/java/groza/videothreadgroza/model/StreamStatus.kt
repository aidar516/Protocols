package groza.videothreadgroza.model

sealed class StreamStatus {
    object Connecting : StreamStatus()
    object Reconnecting : StreamStatus()
    object Playing : StreamStatus()
    object Stopped : StreamStatus()
    object Paused : StreamStatus()
    object Loading : StreamStatus()
    data class Error(val message: String) : StreamStatus()
}
