//package com.byaidar.protocols.rtsp
//
//import android.content.Context
//import android.net.Uri
//import org.videolan.libvlc.LibVLC
//import org.videolan.libvlc.Media
//import org.videolan.libvlc.MediaPlayer
//import org.videolan.libvlc.util.VLCVideoLayout
//
//class RtspStreamHandler(context: Context) {
//    private val libVLC = LibVLC(context, arrayListOf(
//        "--rtsp-tcp",
//        "--network-caching=500",
//        "--demux=ts",
//        "--no-drop-late-frames",
//        "--no-skip-frames",
//        "--rtsp-frame-buffer-size=1000000",
//        "--avcodec-codec=h264",
//        "--file-caching=500",
//        "--live-caching=500",
//        "--clock-jitter=0",
//        "--verbose=2"
//    ))
//    private val mediaPlayer = MediaPlayer(libVLC)
//
//    fun attachView(vlcVideoLayout: VLCVideoLayout) {
//        mediaPlayer.attachViews(vlcVideoLayout, null, false, false)
//    }
//
//    fun playStream(url: String) {
//        val media = Media(libVLC, Uri.parse(url))
//        media.setHWDecoderEnabled(true, false)
//        media.addOption(":network-caching=150")
//        mediaPlayer.media = media
//        media.release()
//        mediaPlayer.play()
//    }
//
//    fun release() {
//        mediaPlayer.stop()
//        mediaPlayer.detachViews()
//        mediaPlayer.release()
//        libVLC.release()
//    }
//}