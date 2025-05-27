package groza.videothreadgroza.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import groza.videothreadgroza.manager.VideoThreadManager
import groza.videothreadgroza.model.VideoThread
import org.videolan.libvlc.util.VLCVideoLayout

@Composable
fun VideoThreadView(
    videoThread: VideoThread,
    videoThreadManager: VideoThreadManager,
    modifier: Modifier
){
    val stableVideoThread = remember(videoThread) { videoThread }

    DisposableEffect(stableVideoThread) {
        videoThreadManager.play(stableVideoThread)
        onDispose {

        }
    }

    when(videoThread){
        is VideoThread.RTMP, is VideoThread.UDP, is VideoThread.UDPMulticast -> {
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        useController = false
                        videoThreadManager.attachView(videoThread, this)
                    }
                },
                modifier = modifier
            )
        }
        is VideoThread.RTSP -> {
            AndroidView(
                factory = {
                    VLCVideoLayout(it).apply {
                        videoThreadManager.attachView(videoThread, this)
                    }
                },
                modifier = modifier
            )
        }
        else -> throw Exception("Unsupported stream")
    }
}