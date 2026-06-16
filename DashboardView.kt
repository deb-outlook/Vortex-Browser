package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdBlocker
import com.example.data.BlockedAdLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardView(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val accent by viewModel.accentColor.collectAsState()
    val activeBlockLevel by viewModel.adBlockLevel.collectAsState()
    val isCookieBlockEnabled by viewModel.cookieBlockEnabled.collectAsState()
    val totalBlockedLifetime by viewModel.totalBlockedCountLifetime.collectAsState()
    val currentBlockedLogs by viewModel.blockedAdLogs.collectAsState()

    val accentColor = Color(accent.hex)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1113))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // 1. Header Area
        item {
            Text(
                text = "THREAT PROTECTION SHIELD",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Configure ad filters, cookie barriers, and visual themes",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // 2. Safety Level Radial Arc Gauge
        item {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    
                    // Track Arc
                    drawArc(
                        color = Color(0xFF1D2024),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
                    )

                    // Fill Arc (Active safety level depending on block choices)
                    val activeSweep = when (activeBlockLevel) {
                        AdBlocker.BlockLevel.STRICT -> 255f
                        AdBlocker.BlockLevel.STANDARD -> 210f
                        else -> 70f
                    }

                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(accentColor.copy(alpha = 0.5f), accentColor)
                        ),
                        startAngle = 135f,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val safetyPct = when (activeBlockLevel) {
                        AdBlocker.BlockLevel.STRICT -> "99.8%"
                        AdBlocker.BlockLevel.STANDARD -> "94.5%"
                        else -> "18.2%"
                    }
                    val safetyLabel = when (activeBlockLevel) {
                        AdBlocker.BlockLevel.STRICT -> "IMPREGNABLE"
                        AdBlocker.BlockLevel.STANDARD -> "SECUREED"
                        else -> "EXPOSED"
                    }

                    Text(
                        text = "SAFETY SCORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = safetyPct,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = safetyLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // 3. Security Settings Board
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
                        text = "FILTER INTENSITY SETTINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    // Three levels selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdBlocker.BlockLevel.values().forEach { level ->
                            val isSelected = activeBlockLevel == level
                            val label = when (level) {
                                AdBlocker.BlockLevel.DISABLED -> "OFF (RAW)"
                                AdBlocker.BlockLevel.STANDARD -> "STANDARD"
                                AdBlocker.BlockLevel.STRICT -> "ULTRA STRICT"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) accentColor else Color(0x1AFFFFFF))
                                    .clickable { viewModel.setAdBlockLevel(level) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.Black else Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0x0CFFFFFF), thickness = 1.dp)

                    // Cookie Block toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Block Trackers & Script Beacons", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Interceptors telemetry and third-party tracking", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isCookieBlockEnabled,
                            onCheckedChange = { viewModel.setCookieBlock(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0x1AFFFFFF)
                            )
                        )
                    }
                }
            }
        }

        // 4. Custom 3D Accent Theme swapper
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1D2024), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "3D NEON THEME CUSTOMIZATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VortexAccent.values().forEach { accentSlot ->
                            val isSelected = accent == accentSlot
                            val colorVal = Color(accentSlot.hex)
                            
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(colorVal.copy(alpha = 0.15f))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) colorVal else Color(0x1AFFFFFF),
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setAccent(accentSlot) }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colorVal)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Block logs Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE INTERCEPT LOGS (${currentBlockedLogs.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )

                if (currentBlockedLogs.isNotEmpty()) {
                    Text(
                        text = "CLEAR LOGS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { viewModel.clearBlockedLogs() }
                            .padding(4.dp)
                    )
                }
            }
        }

        // Intercept logs list
        if (currentBlockedLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFF1D2024), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AllInbox,
                            contentDescription = "Empty logs",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No ads blocked during this session, clean skies!",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(currentBlockedLogs) { log ->
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val timeStr = sdf.format(Date(log.timestamp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1D2024), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2C2F44)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Blocked ad icon",
                                tint = Color(0xFFE57373),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.host,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "Origin: ${log.pageUrl}",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = timeStr,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
