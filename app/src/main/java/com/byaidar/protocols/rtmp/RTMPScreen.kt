package com.byaidar.protocols.rtmp

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun RTMPScreen(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = Color(0xFFD5D5D5)
    val useDarkIcons = true

    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = useDarkIcons
        )
    }

    var offset by remember { mutableStateOf(Offset(100f, 100f)) }
    var windowSize by remember { mutableStateOf(200.dp) }

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

//    var rtmpCamera: RtmpCamera2? by remember { mutableStateOf(null) }
//    var isStreaming by remember { mutableStateOf(false) }

    val rtmpUrl = remember {"rtmp://192.168.19.250:1935/live/android"}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(statusBarColor)
    ) {
        val windowSizePx = with(density) { windowSize.toPx() }
        val maxOffsetX = max(0f, screenWidthPx - windowSizePx)
        val maxOffsetY = max(0f, screenHeightPx - windowSizePx)

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        offset.x.coerceIn(0f, maxOffsetX).roundToInt(),
                        offset.y.coerceIn(0f, maxOffsetY).roundToInt()
                    )
                }
                .size(windowSize)
                .background(Color.White, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clipToBounds()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset = Offset(
                            (offset.x + dragAmount.x).coerceIn(0f, maxOffsetX),
                            (offset.y + dragAmount.y).coerceIn(0f, maxOffsetY)
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newSize = (windowSize * zoom).coerceIn(100.dp, 420.dp)
                        val newSizePx = with(density) { newSize.toPx() }

                        val newMaxOffsetX = max(0f, screenWidthPx - newSizePx)
                        val newMaxOffsetY = max(0f, screenHeightPx - newSizePx)

                        offset = Offset(
                            (offset.x + pan.x).coerceIn(0f, newMaxOffsetX),
                            (offset.y + pan.y).coerceIn(0f, newMaxOffsetY)
                        )
                        windowSize = newSize
                    }
                }
        ) {
//            StreamUI(
//                isStreaming = { isStreaming },
//                onStartStop = {
//                    if (rtmpCamera == null) return@StreamUI
//
//                    try {
//                        rtmpCamera?.prepareVideo(1280, 720, 30, 1200 * 1024, 2, 90)
//                        rtmpCamera?.startStream("rtmp://192.168.19.234:1935/live/android")
//                        isStreaming = true
//                    } catch (e: Exception) {
//                        Log.e("RTMP", "Stream prep failed", e)
//                    }
//                },
//                onSwitchCamera = {
//                    rtmpCamera?.switchCamera()
//                },
//                onViewCreated = { view ->
//                    view.setAspectRatioMode(AspectRatioMode.Adjust)
//                    view.setEncoderSize(1280, 720)
//
//                    rtmpCamera = RtmpCamera2(
//                        view,
//                        object : ConnectChecker {
//                            override fun onConnectionSuccess() {
//                                Log.d("RTMP", "Успешное подключение!")
//                            }
//
//                            override fun onConnectionFailed(reason: String) {
//                                Log.e("RTMP", "Ошибка: $reason")
//                            }
//
//                            override fun onConnectionStarted(url: String) {
//                                Log.d("RTMP", "Подключение начато. URL: $url")
//                            }
//
//                            override fun onDisconnect() {
//                                Log.d("RTMP", "Отключено")
//                                isStreaming = false
//                            }
//
//                            override fun onAuthError() {
//                                Log.e("RTMP", "Ошибка авторизации")
//                            }
//
//                            override fun onAuthSuccess() {
//                                Log.d("RTMP", "Авторизация успешна")
//                            }
//                        }
//                    )
//                },
//                windowSize = windowSize
//            )
            RTMPVideoPlayer(
                rtmpUrl = rtmpUrl,
                modifier = Modifier.fillMaxSize()
            )
        }

        Button(
            onClick = {
                navController.navigate("main")
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Text("Back")
        }
    }

//    DisposableEffect(Unit) {
//        onDispose {
//            rtmpCamera?.stopStream()
//            rtmpCamera?.glInterface?.stop()
//        }
//    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRTMPScreen(){
    val fakeNavController = rememberNavController()

    RTMPScreen(fakeNavController)
}