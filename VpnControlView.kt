package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VpnProtocol
import com.example.data.VpnServer
import com.example.data.VpnState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VpnControlView(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val accent by viewModel.accentColor.collectAsState()
    val vpnState by viewModel.vpnState.collectAsState()
    val selectedServer by viewModel.currentVpnServer.collectAsState()
    val activeProtocol by viewModel.vpnProtocol.collectAsState()
    val dlSpeed by viewModel.vpnDownloadSpeed.collectAsState()
    val ulSpeed by viewModel.vpnUploadSpeed.collectAsState()
    val totalMb by viewModel.vpnBandwidthMb.collectAsState()
    val durationSeconds by viewModel.vpnDurationSeconds.collectAsState()
    val ipAddress by viewModel.assignedVpnIp.collectAsState()

    val accentColor = Color(accent.hex)

    // Animation configuration for 3D Pulse Sphere
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_sphere_transition")
    
    // Radiating waves when tunneling
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = if (vpnState != VpnState.DISCONNECTED) 1.0f else 0.0f,
        targetValue = if (vpnState != VpnState.DISCONNECTED) 2.2f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_circle_1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = if (vpnState != VpnState.DISCONNECTED) 0.6f else 0.0f,
        targetValue = if (vpnState != VpnState.DISCONNECTED) 0.0f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha_1"
    )

    // Orbit coordinates rotational offset
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1113))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // 1. Title Banner
        item {
            Text(
                text = "QUANTUM VPN PORTAL",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Securing browser data flow with military tunnels",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // 2. Beautiful 3D Pulse Power Button
        item {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                
                // Pulsing Rings Canvas Overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radiusBase = size.width * 0.3f
                    
                    // Wave Pulse 1
                    if (vpnState != VpnState.DISCONNECTED) {
                        drawCircle(
                            color = accentColor,
                            radius = radiusBase * pulseScale1,
                            style = Stroke(3.dp.toPx()),
                            alpha = pulseAlpha1,
                            center = center
                        )
                    }

                    // Rotating Orbital secure particles representing tunneling nodes
                    val orbitRadius = radiusBase * 1.5f
                    val angleRad = Math.toRadians(rotationAngle.toDouble())
                    val particleX1 = center.x + orbitRadius * cos(angleRad).toFloat()
                    val particleY1 = center.y + orbitRadius * sin(angleRad).toFloat()
                    
                    val particleX2 = center.x - orbitRadius * cos(angleRad).toFloat()
                    val particleY2 = center.y - orbitRadius * sin(angleRad).toFloat()

                    // Draw outer orbiting ring trace
                    drawCircle(
                        color = accentColor.copy(alpha = 0.15f),
                        radius = orbitRadius,
                        style = Stroke(1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))),
                        center = center
                    )

                    // Draw particles
                    if (vpnState == VpnState.CONNECTED) {
                        drawCircle(
                            color = accentColor,
                            radius = 6.dp.toPx(),
                            center = Offset(particleX1, particleY1)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(particleX2, particleY2)
                        )
                    }
                }

                // Main Touch Power Sphere
                val sphereGlowSize = if (vpnState == VpnState.CONNECTED) 16.dp else 4.dp
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clickable { viewModel.vpnManager.toggleVpn() }
                        .border(
                            2.dp,
                            Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.5f), accentColor.copy(alpha = 0.5f))
                            ),
                            RoundedCornerShape(65.dp)
                        )
                        .background(
                            Brush.radialGradient(
                                colors = when (vpnState) {
                                    VpnState.CONNECTED -> listOf(accentColor.copy(alpha = 0.3f), Color(0xFF1D2024))
                                    VpnState.CONNECTING -> listOf(Color(0x33FF9800), Color(0xFF1D2024))
                                    else -> listOf(Color(0x1AFFFFFF), Color(0xFF1D2024))
                                }
                            ),
                            RoundedCornerShape(65.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (vpnState) {
                                VpnState.CONNECTED -> Icons.Default.VpnLock
                                VpnState.CONNECTING -> Icons.Default.Sync
                                else -> Icons.Default.PowerSettingsNew
                            },
                            contentDescription = "VPN toggle",
                            tint = when (vpnState) {
                                VpnState.CONNECTED -> accentColor
                                VpnState.CONNECTING -> Color(0xFFFF9800)
                                else -> Color.Gray
                            },
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (vpnState) {
                                VpnState.CONNECTED -> "SECURE ON"
                                VpnState.CONNECTING -> "TUNNELING..."
                                else -> "CONNECT"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // 3. Simulated Connection Statistics Grid
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1D2024), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "TUNNEL DIAGNOSTICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Assigned Virtual IP", fontSize = 11.sp, color = Color.Gray)
                            Text(ipAddress, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Active Protocol", fontSize = 11.sp, color = Color.Gray)
                            Text(activeProtocol.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }
                    }

                    // Divider lines
                    HorizontalDivider(color = Color(0x0CFFFFFF), thickness = 1.dp)

                    // Speed indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Download Card
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Download Speed", fontSize = 11.sp, color = Color.Gray)
                                Text(String.format("%.1f MB/s", dlSpeed), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Custom speed fill bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0x1AFFFFFF))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = (dlSpeed / 18.0f).coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .background(accentColor)
                                )
                            }
                        }

                        // Upload Card
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Upload Speed", fontSize = 11.sp, color = Color.Gray)
                                Text(String.format("%.1f MB/s", ulSpeed), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Custom speed fill bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0x1AFFFFFF))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = (ulSpeed / 8.0f).coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .background(Color(0xFF90CAF9))
                                )
                            }
                        }
                    }

                    // Divider
                    HorizontalDivider(color = Color(0x0CFFFFFF), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Secure Data Routed", fontSize = 11.sp, color = Color.Gray)
                            Text(String.format("%.2f MB", totalMb), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Tunnel Uptime", fontSize = 11.sp, color = Color.Gray)
                            val minutes = durationSeconds / 60
                            val seconds = durationSeconds % 60
                            Text(String.format("%02d:%02d", minutes, seconds), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // 4. Server Nodes Selector
        item {
            Text(
                text = "SECURE TUNNEL LOCATIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                textAlign = TextAlign.Start
            )
        }

        items(viewModel.vpnManager.servers) { server ->
            val isSelected = selectedServer.ipAddress == server.ipAddress
            
            // Generate flag emoji
            val flagEmoji = when (server.countryCode) {
                "US" -> "🇺🇸"
                "JP" -> "🇯🇵"
                "CH" -> "🇨🇭"
                "DE" -> "🇩🇪"
                "IS" -> "🇮🇸"
                "SG" -> "🇸🇬"
                else -> "🌐"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (isSelected) 6.dp else 2.dp, RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) Color(0xFF232931) else Color(0xFF1D2024),
                        RoundedCornerShape(14.dp)
                    )
                    .border(
                        1.dp,
                        if (isSelected) accentColor else Color(0x0CFFFFFF),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { viewModel.vpnManager.selectServer(server) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Country Flag Circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0F1113)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = flagEmoji, fontSize = 20.sp)
                    }

                    // Server Specs
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = server.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = server.securityLevel,
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }

                    // Latency / Status Dot
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val dotColor = when {
                                server.latencyMs < 30 -> Color(0xFF4CAF50)
                                server.latencyMs < 60 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(dotColor)
                            )
                            Text(
                                text = "${server.latencyMs} ms",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "PORT SELECT",
                            fontSize = 9.sp,
                            color = if (isSelected) accentColor else Color.Gray,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Protocol selector HUD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .background(Color(0xFF1D2024), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TUNNELING ENCRYPTION STANDARD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VpnProtocol.values().forEach { proto ->
                            val isSelected = activeProtocol == proto
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) accentColor else Color(0x1AFFFFFF))
                                    .clickable { viewModel.vpnManager.setProtocol(proto) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = proto.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
