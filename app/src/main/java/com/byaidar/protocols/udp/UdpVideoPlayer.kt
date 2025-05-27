//package com.byaidar.protocols.udp
//
//import android.util.Log
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
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.exoplayer.DefaultLoadControl
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.exoplayer.source.ProgressiveMediaSource
//import androidx.media3.ui.PlayerView
//import com.byaidar.protocols.udp.multicast.UdpDataSourceFactory
//
//@OptIn(UnstableApi::class)
//@Composable
//fun UDPVideoPlayer(uri: String, modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//
//    val exoPlayer = remember {
//        val loadControl = DefaultLoadControl.Builder()
//            .setBufferDurationsMs(
//                1500,
//                3000,
//                500,
//                1000
//            )
//            .build()
//
//        val mediaItem = MediaItem.fromUri(uri)
//        val udpDataSourceFactory = UdpDataSourceFactory()
//        val mediaSource = ProgressiveMediaSource.Factory(udpDataSourceFactory)
//            .createMediaSource(mediaItem)
//
//        val player = ExoPlayer.Builder(context)
//            .setLoadControl(loadControl)
//            .build()
//
//        player.setMediaSource(mediaSource)
//        player.prepare()
//        player.playWhenReady = true
//
//        player.addListener(object : Player.Listener {
//            override fun onPlaybackStateChanged(state: Int) {
//                val stateString = when (state) {
//                    Player.STATE_IDLE -> "IDLE"
//                    Player.STATE_BUFFERING -> "BUFFERING"
//                    Player.STATE_READY -> "READY"
//                    Player.STATE_ENDED -> "ENDED"
//                    else -> "UNKNOWN"
//                }
//                Log.d("UDPPlayer", "Playback state changed: $stateString")
//            }
//
//            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                Log.d("UDPPlayer", "Is playing: $isPlaying")
//            }
//
//            override fun onPlayerError(error: PlaybackException) {
//                Log.e("UDPPlayer", "Playback error: ${error.message}")
//            }
//        })
//
//        player
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
//        factory = {
//            PlayerView(context).apply {
//                useController = false
//                player = exoPlayer
//            }
//        }
//    )
//}