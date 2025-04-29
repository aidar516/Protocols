package com.byaidar.protocols

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.pedro.library.view.OpenGlView

@Composable
fun StreamUI(
    isStreaming: () -> Boolean,
    onStartStop: (OpenGlView) -> Unit,
    onSwitchCamera: () -> Unit,
    onViewCreated: (OpenGlView) -> Unit,
    windowSize: Dp
) {
    var openGlView: OpenGlView? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFF3F3F3))
        ) {
            AndroidView(
                factory = { context ->
                    OpenGlView(context).also {
                        openGlView = it
                        onViewCreated(it)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    openGlView?.let { onStartStop(it) }
                },
                modifier = Modifier
                    .height(windowSize * 0.12f)
                    .weight(1f)
            ) {
                Text(
                    text = if (isStreaming()) "Остановить" else "Старт",
                    fontSize = 16.sp
                )
            }

            Button(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .height(windowSize * 0.12f)
                    .weight(1f)
            ) {
                Text("Сменить камеру", fontSize = 14.sp, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

