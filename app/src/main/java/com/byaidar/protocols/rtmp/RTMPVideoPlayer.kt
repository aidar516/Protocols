//package com.byaidar.protocols.rtmp
//
//import androidx.annotation.OptIn
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.media3.common.MediaItem
//import androidx.media3.common.PlaybackException
//import androidx.media3.common.Player
//import androidx.media3.common.util.Log
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.datasource.rtmp.RtmpDataSource
//import androidx.media3.exoplayer.DefaultLoadControl
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.exoplayer.source.ProgressiveMediaSource
//import androidx.media3.ui.PlayerView
//
//@OptIn(UnstableApi::class)
//@Composable
//fun RTMPVideoPlayer(rtmpUrl: String, modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//
//    val exoPlayer = remember {
//        ExoPlayer.Builder(context)
//            .setLoadControl(
//                DefaultLoadControl.Builder()
//                    .setBufferDurationsMs(2000, 5000, 1000, 2000)
//                    .build()
//            )
//            .build().apply {
//                val dataSourceFactory = RtmpDataSource.Factory()
//                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//                    .createMediaSource(MediaItem.fromUri(rtmpUrl))
//
//                setMediaSource(mediaSource)
//                repeatMode = Player.REPEAT_MODE_ONE
//                prepare()
//                playWhenReady = true
//
//                addListener(object : Player.Listener {
//                    override fun onPlaybackStateChanged(state: Int) {
//                        when (state) {
//                            Player.STATE_READY -> Log.d("RTMPPlayer", "Playback ready")
//                            Player.STATE_BUFFERING -> Log.d("RTMPPlayer", "Buffering...")
//                            Player.STATE_ENDED -> Log.d("RTMPPlayer", "Playback ended")
//                            Player.STATE_IDLE -> Log.d("RTMPPlayer", "Idle state")
//                        }
//                    }
//
//                    override fun onPlayerError(error: PlaybackException) {
//                        Log.e("RTMPPlayer", "Error: ${error.errorCodeName}", error)
//                    }
//                })
//            }
//    }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            exoPlayer.release()
//        }
//    }
//
//    AndroidView(
//        modifier = modifier,
//        factory = { context ->
//            PlayerView(context).apply {
//                useController = false
//                player = exoPlayer
//            }
//        }
//    )
//}