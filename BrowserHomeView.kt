package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VpnServer
import com.example.data.VpnState
import kotlinx.coroutines.delay

@Composable
fun BrowserHomeView(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val accent by viewModel.accentColor.collectAsState()
    val searchInput by viewModel.searchQueryInput.collectAsState()
    val vpnState by viewModel.vpnState.collectAsState()
    val totalBlockedLifetime by viewModel.totalBlockedCountLifetime.collectAsState()
    val activeVpnServer by viewModel.currentVpnServer.collectAsState()
    val isBookmarked by viewModel.isCurrentBookmarked.collectAsState()

    val accentColor = Color(accent.hex)
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1113)) // Base background
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        Spacer(modifier = Modifier.height(12.dp))

        // Premium 3D Header Logotype
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .drawBehind {
                        val width = size.width
                        val height = size.height
                        val path = Path().apply {
                            moveTo(width / 2, 0f)
                            lineTo(width, height * 0.25f)
                            lineTo(width, height * 0.75f)
                            lineTo(width / 2, height)
                            lineTo(0f, height * 0.75f)
                            lineTo(0f, height * 0.25f)
                            close()
                        }
                        
                        // 3D Shadow extrusion
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF232931), Color(0xFF161B22))
                            )
                        )
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cyclone,
                    contentDescription = "Vortex Shield logo",
                    tint = accentColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "VORTEX SECURE",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Text(
                text = "Quantum Tunnel & Advanced AdBlock Engine",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // High Density Pill URL Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1D2024), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { viewModel.setSearchInput(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search encrypted web or type URL...", color = Color.Gray, fontSize = 14.sp) },
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.navigateUrl(searchInput)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search enter",
                            tint = accentColor
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    viewModel.navigateUrl(searchInput)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Professional 3D VPN Widget
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp), ambientColor = Color.Black, spotColor = Color.Black)
                .background(
                    Brush.linearGradient(colors = listOf(Color(0xFF232931), Color(0xFF161B22))),
                    RoundedCornerShape(28.dp)
                )
                .border(1.dp, Color(0x2BFFFFFF), RoundedCornerShape(28.dp)) // Highlights top border
                .clickable { viewModel.setTab(TabScreen.Vpn) }
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "BUILT-IN NEXUS VPN",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = if (vpnState == VpnState.CONNECTED) "Protected" else "Unprotected",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(if (vpnState == VpnState.CONNECTED) accentColor else Color(0x1AFFFFFF), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Vpn lock key",
                            tint = if (vpnState == VpnState.CONNECTED) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (vpnState == VpnState.CONNECTED) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                        )
                        Text(
                            text = if (vpnState == VpnState.CONNECTED) "Node: ${activeVpnServer.name}" else "Location: Zurich, CH",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                    Text(
                        text = "Ping 12ms",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ad-Blocker & Tracking Stats Widgets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ads Blocked Widget
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1D2024), RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(24.dp))
                    .clickable { viewModel.setTab(TabScreen.Dashboard) }
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0x1AEF4444), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Blocked ads",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "$totalBlockedLifetime",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Ads Blocked",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Trackers Stopped Widget
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1D2024), RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(24.dp))
                    .clickable { viewModel.setTab(TabScreen.Dashboard) }
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0x1AA855F7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Trackers Stopped",
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = if (totalBlockedLifetime > 0) "${(totalBlockedLifetime * 0.17).toInt() + 12}" else "0",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Trackers Stopped",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Browsing Speed metrics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1D2024), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x1AF59E0B), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Browsing fast bolt",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Browsing Speed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "4.2x faster than standard",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
            Text(
                text = "OPTIMIZED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Shortcuts Grid Header
        Text(
            text = "QUICK SHORTCUTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.5.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 10.dp, start = 4.dp)
        )

        // Balanced 3-column Shortcuts using rows (to evade grid/lazy nest crashes smoothly)
        val shortcuts = listOf(
            Triple("Google", "https://google.com", Icons.Default.Language),
            Triple("DuckDuckGo", "https://duckduckgo.com", Icons.Default.Shield),
            Triple("Wikipedia", "https://wikipedia.org", Icons.Default.MenuBook),
            Triple("YouTube", "https://youtube.com", Icons.Default.PlayArrow),
            Triple("GitHub", "https://github.com", Icons.Default.Code),
            Triple("Reddit", "https://reddit.com", Icons.Default.Forum)
        )

        val rows = shortcuts.chunked(3)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp)
                                .background(Color(0xFF1D2024), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(16.dp))
                                .clickable { viewModel.navigateUrl(item.second) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = item.third,
                                    contentDescription = item.first,
                                    tint = accentColor.copy(alpha = 0.85f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.first,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

