package com.byaidar.protocols.rtmp

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import groza.videothreadgroza.internet.AndroidConnectivityObserver
import groza.videothreadgroza.manager.VideoThreadManager
import groza.videothreadgroza.model.StreamStatus
import groza.videothreadgroza.model.VideoThread
import groza.videothreadgroza.ui.VideoThreadView
import kotlin.math.max
import kotlin.math.roundToInt

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun RTMPScreen(navController: NavController) {
    val context = LocalContext.current
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

    var inputUrl by remember { mutableStateOf("") }
    var activeUrl by remember { mutableStateOf<String?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    val connectivityObserver = remember {
        AndroidConnectivityObserver(context)
    }

    val videoThreadManager = remember {
        VideoThreadManager(
            context = context,
            lifecycle = lifecycleOwner.lifecycle,
            connectivityObserver = connectivityObserver
        )
    }

    val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)

    val streamStatus by videoThreadManager.streamStatus.collectAsState()

    var isFirstStatus by remember { mutableStateOf(true) }

    var lastStatus by remember { mutableStateOf<StreamStatus?>(null) }

    LaunchedEffect(streamStatus) {
        if (isFirstStatus) {
            isFirstStatus = false
        } else {
            if (streamStatus != lastStatus) {
                lastStatus = streamStatus
                Toast.makeText(context, getStatusMessage(streamStatus), Toast.LENGTH_SHORT).show()
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(statusBarColor)
    ) {
        val windowSizePx = with(density) { windowSize.toPx() }
        val maxOffsetX = max(0f, screenWidthPx - windowSizePx)
        val maxOffsetY = max(0f, screenHeightPx - windowSizePx)

        if (!isConnected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red)
                    .padding(8.dp)
                    .align(Alignment.TopCenter)
            ) {
                Text(
                    text = "Нет подключения к сети",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (activeUrl != null) {

            Log.i("RTMPScreen", "$activeUrl")

            val videoThread = remember(activeUrl) {
                when {
                    activeUrl!!.startsWith("rtmp://") -> VideoThread.RTMP(url = activeUrl!!, password = null)
                    activeUrl!!.startsWith("rtsp://") -> VideoThread.RTSP(url = activeUrl!!, login = null, password = null)
                    activeUrl!!.startsWith("udp://@") -> VideoThread.UDPMulticast(url = activeUrl!!, password = null)
                    activeUrl!!.startsWith("udp://") -> VideoThread.UDP(url = activeUrl!!, password = null)
                    else -> null
                }
            }

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
                if (videoThread != null) {
                    VideoThreadView(
                        videoThread = videoThread,
                        videoThreadManager = videoThreadManager,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (streamStatus == StreamStatus.Reconnecting) {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            CircularProgressIndicator()
                        }
                    }

                    Button(
                        onClick = {
                            videoThreadManager.destroy()
                            activeUrl = null
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Text("Стоп")
                    }
                }
            }
        }

        OutlinedTextField(
            value = inputUrl,
            onValueChange = { inputUrl = it },
            label = {
                Text(
                    text = "Url",
                    color = Color.Black
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    if (inputUrl.isNotBlank()) {
                        activeUrl = inputUrl
                        inputUrl = ""
                    } else {
                        Toast.makeText(context, "Некорректный URL", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Play")
                }

            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
        )

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
}

fun getStatusMessage(status: StreamStatus): String = when(status){
    is StreamStatus.Playing -> "Поток воспроизводится"
    is StreamStatus.Stopped -> "Поток остановлен"
    is StreamStatus.Error -> "Ошибка: $status"
    is StreamStatus.Reconnecting -> "Переподключение..."
    is StreamStatus.Paused -> "Поток на паузе"
    is StreamStatus.Connecting -> "Подключение..."
    is StreamStatus.Loading -> "Загрузка потока..."
}
