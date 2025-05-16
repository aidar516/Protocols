package com.byaidar.protocols.udp.multicast

import androidx.annotation.OptIn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun UDPMulticastScreen(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = Color(0xFFD5D5D5)
    val useDarkIcons = true

    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = useDarkIcons
        )
    }

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    var offset by remember { mutableStateOf(Offset(100f, 100f)) }
    var windowSize by remember { mutableStateOf(200.dp) }

    var inputUrl by remember { mutableStateOf("") }
    var activeUrl by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(statusBarColor)
    ) {
        val windowSizePx = with(density) { windowSize.toPx() }
        val maxOffsetX = max(0f, screenWidthPx - windowSizePx)
        val maxOffsetY = max(0f, screenHeightPx - windowSizePx)

        if(activeUrl != null){
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
                UDPMulticastVideoPlayer(
                    uri = activeUrl!!,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        OutlinedTextField(
            value = inputUrl,
            onValueChange = { inputUrl = it },
            label = {
                Text(
                    text = "UDP Multicast URL",
                    color = Color.Black
                )
            },
            trailingIcon = {
                IconButton(onClick = { activeUrl = inputUrl }) {
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



@Preview(showBackground = true)
@Composable
fun PreviewUDPMulticastScreen(){
    val fakeNavController = rememberNavController()

    UDPMulticastScreen(fakeNavController)
}