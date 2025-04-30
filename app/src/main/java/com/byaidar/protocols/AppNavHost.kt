package com.byaidar.protocols

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.byaidar.protocols.rtmp.RTMPScreen
import com.byaidar.protocols.rtsp.RTSPScreen
import com.byaidar.protocols.udp.multicast.UDPMulticastScreen

@Composable
fun AppNavHost(){
    val navController = rememberNavController()
    var hasPermissions by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(navController)
        }
        composable("rtmp") {
            RTMPScreen(navController)

        }
        composable("udp-multicast"){
            UDPMulticastScreen(navController)
        }
        composable("rtsp"){
            RTSPScreen(navController)
        }
        composable("udp"){

        }
    }
}
