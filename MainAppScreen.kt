package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VpnState

@Composable
fun MainAppScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val accent by viewModel.accentColor.collectAsState()
    val currentUrl by viewModel.currentUrl.collectAsState()
    val pageTitle by viewModel.currentTitle.collectAsState()
    val searchInput by viewModel.searchQueryInput.collectAsState()
    val vpnState by viewModel.vpnState.collectAsState()
    val sessionBlocked by viewModel.sessionBlockedCount.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    val canGoForward by viewModel.canGoForward.collectAsState()

    val accentColor = Color(accent.hex)
    val keyboardController = LocalSoftwareKeyboardController.current

    val isHome = currentUrl == "vortex://home"

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1113)),
        contentWindowInsets = WindowInsets.safeDrawing, // Perfect Edge-to-Edge handling
        bottomBar = {
            // High Density Modern Solid Glass Bottom Navbar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1113)) // Outer background bounds
                    .navigationBarsPadding() // Mandated Safe Areas Padding
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(Color(0xFF1D2024))
                        .border(1.dp, Color(0x0CFFFFFF))
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item 1: Browser Tab
                    NavbarItem(
                        selected = activeTab == TabScreen.Browser,
                        iconSelected = Icons.Default.Public,
                        iconUnselected = Icons.Default.Public,
                        label = "Web",
                        accentColor = accentColor,
                        onClick = { viewModel.setTab(TabScreen.Browser) }
                    )

                    // Item 2: Quantum VPN Control Portal
                    NavbarItem(
                        selected = activeTab == TabScreen.Vpn,
                        iconSelected = Icons.Default.VpnLock,
                        iconUnselected = Icons.Default.VpnLock,
                        label = "VPN",
                        accentColor = accentColor,
                        onClick = { viewModel.setTab(TabScreen.Vpn) }
                    )

                    // Item 3: AdBlock Control Panel
                    NavbarItem(
                        selected = activeTab == TabScreen.Dashboard,
                        iconSelected = Icons.Default.Shield,
                        iconUnselected = Icons.Default.Shield,
                        label = "Shield",
                        accentColor = accentColor,
                        onClick = { viewModel.setTab(TabScreen.Dashboard) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F1113))
        ) {
            
            // Render active Web browser top URL toolbar ONLY if we are viewing a real page (non-homepage) in Browser Tab
            if (activeTab == TabScreen.Browser && !isHome) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1D2024))
                        .border(width = 1.dp, color = Color(0x0CFFFFFF))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    
                    // Left navigation buttons: Back, Home
                    IconButton(
                        onClick = { viewModel.systemWebViewRef?.goBack() },
                        enabled = canGoBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Web back",
                            tint = if (canGoBack) Color.White else Color.DarkGray
                        )
                    }

                    IconButton(
                        onClick = { viewModel.changeUrl("vortex://home") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Web home",
                            tint = accentColor
                        )
                    }

                    // Centered input address slot with dynamic states
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .background(Color(0xFF0F1113), RoundedCornerShape(24.dp))
                            .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(24.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Secure padlock or warning prefix
                            Icon(
                                imageVector = if (vpnState == VpnState.CONNECTED) Icons.Default.VpnKey else Icons.Default.Lock,
                                contentDescription = "Secure lock icon",
                                tint = if (vpnState == VpnState.CONNECTED) accentColor else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            
                            // Edit URL inline
                            BasicTextFieldInline(
                                value = searchInput,
                                onValueChange = { viewModel.setSearchInput(it) },
                                onEnter = {
                                    keyboardController?.hide()
                                    viewModel.navigateUrl(searchInput)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Active blocked ads visual node/pill
                            if (sessionBlocked > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFEF4444))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$sessionBlocked blocked",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // Reload/Stop
                    IconButton(
                        onClick = { viewModel.systemWebViewRef?.reload() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Web reload",
                            tint = Color.White
                        )
                    }

                    // Star bookmark toggle
                    IconButton(
                        onClick = { viewModel.toggleBookmark() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        val isBookmarked by viewModel.isCurrentBookmarked.collectAsState()
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Bookmark toggle",
                            tint = if (isBookmarked) accentColor else Color.Gray
                        )
                    }
                }
            }

            // Central View swapper with crossfading fluid motion transitions
            Crossfade(
                targetState = activeTab,
                animationSpec = tween(250),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "page_transitions"
            ) { target ->
                when (target) {
                    TabScreen.Browser -> {
                        if (isHome) {
                            BrowserHomeView(viewModel = viewModel)
                        } else {
                            BrowserWebView(viewModel = viewModel)
                        }
                    }
                    TabScreen.Vpn -> {
                        VpnControlView(viewModel = viewModel)
                    }
                    TabScreen.Dashboard -> {
                        DashboardView(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.NavbarItem(
    selected: Boolean,
    iconSelected: androidx.compose.ui.graphics.vector.ImageVector,
    iconUnselected: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val sizeAnim by animateDpAsState(
            targetValue = if (selected) 24.dp else 20.dp,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "iconSize"
        )
        val alphaAnim by animateFloatAsState(
            targetValue = if (selected) 1.0f else 0.5f,
            label = "iconAlpha"
        )

        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 32.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) accentColor.copy(alpha = 0.15f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) iconSelected else iconUnselected,
                contentDescription = label,
                tint = if (selected) accentColor else Color.Gray,
                modifier = Modifier
                    .size(sizeAnim)
                    .clip(CircleShape)
            )
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (selected) Color.White else Color.Gray,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun BasicTextFieldInline(
    value: String,
    onValueChange: (String) -> Unit,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onEnter() }),
        cursorBrush = Brush.verticalGradient(listOf(Color.White, Color.White))
    )
}
