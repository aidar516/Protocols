//package com.byaidar.protocols.udp.multicast
//
//import android.util.Log
//import androidx.annotation.OptIn
//import androidx.compose.runtime.*
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
//import androidx.media3.exoplayer.upstream.DefaultAllocator
//import androidx.media3.ui.PlayerView
//
//@OptIn(UnstableApi::class)
//@Composable
//fun UDPMulticastVideoPlayer(uri: String, modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//
//    val exoPlayer = remember {
//        val loadControl = DefaultLoadControl.Builder()
//            .setAllocator(DefaultAllocator(true, 16))
//            .setBufferDurationsMs(1000, 2000, 500, 1000)
//            .build()
//
//        val player = ExoPlayer.Builder(context)
//            .setLoadControl(loadControl)
//            .build()
//
//        val mediaItem = MediaItem.fromUri(uri)
//
//        val udpDataSourceFactory = UdpDataSourceFactory()
//        val mediaSource = ProgressiveMediaSource.Factory(udpDataSourceFactory)
//            .createMediaSource(mediaItem)
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
