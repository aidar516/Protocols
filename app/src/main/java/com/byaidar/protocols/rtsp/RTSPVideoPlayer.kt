//package com.byaidar.protocols.rtsp
//
//import androidx.annotation.OptIn
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.media3.common.util.UnstableApi
//import org.videolan.libvlc.util.VLCVideoLayout
//
//@OptIn(UnstableApi::class)
//@Composable
//fun RTSPVideoPlayer(uri: String, modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//    val mediaPlayerHolder = remember { RtspStreamHandler(context) }
//
//    DisposableEffect(uri) {
//        mediaPlayerHolder.playStream(uri)
//        onDispose {
//            mediaPlayerHolder.release()
//        }
//    }
//
//    AndroidView(
//        modifier = modifier,
//        factory = { ctx ->
//            val vlcLayout = VLCVideoLayout(ctx)
//            mediaPlayerHolder.attachView(vlcLayout)
//            vlcLayout
//        }
//    )
//}