package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class VpnServer(
    val name: String,
    val countryCode: String, // e.g. "US", "JP", "CH"
    val ipAddress: String,
    val latencyMs: Int,
    val securityLevel: String,
    val isPremium: Boolean = false
)

enum class VpnProtocol {
    WIREGUARD,
    OPENVPN,
    SHADOWSOCKS,
    VORTEX_SHIELD
}

enum class VpnState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTIONS
}

class VpnManager(private val externalScope: CoroutineScope) {

    val servers = listOf(
        VpnServer("Zurich SafeHaven", "CH", "109.202.107.4", 24, "Military Grade (AES-256)"),
        VpnServer("Reykjavik Glacier", "IS", "185.112.146.12", 48, "Chacha20 Quantum-Safe"),
        VpnServer("Tokyo Stealth", "JP", "210.140.10.85", 85, "Shadowsocks Obfuscation"),
        VpnServer("New York Gateway", "US", "192.241.140.231", 12, "WireGuard Express"),
        VpnServer("Frankfurt Hub", "DE", "46.165.2.14", 32, "Double Hop AES-256"),
        VpnServer("Singapore Apex", "SG", "128.199.112.44", 64, "VortexShield Stealth-Stealth")
    )

    private val _selectedServer = MutableStateFlow(servers[0])
    val selectedServer: StateFlow<VpnServer> = _selectedServer.asStateFlow()

    private val _protocol = MutableStateFlow(VpnProtocol.VORTEX_SHIELD)
    val protocol: StateFlow<VpnProtocol> = _protocol.asStateFlow()

    private val _vpnState = MutableStateFlow(VpnState.DISCONNECTED)
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private val _downloadSpeed = MutableStateFlow(0.0f) // MB/s
    val downloadSpeed: StateFlow<Float> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow(0.0f) // MB/s
    val uploadSpeed: StateFlow<Float> = _uploadSpeed.asStateFlow()

    private val _bandwidthUsedMb = MutableStateFlow(0.0f)
    val bandwidthUsedMb: StateFlow<Float> = _bandwidthUsedMb.asStateFlow()

    private val _sessionDurationSeconds = MutableStateFlow(0)
    val sessionDurationSeconds: StateFlow<Int> = _sessionDurationSeconds.asStateFlow()

    private val _assignedIp = MutableStateFlow("Original Carrier IP")
    val assignedIp: StateFlow<String> = _assignedIp.asStateFlow()

    private var telemetryJob: Job? = null
    private var connectionJob: Job? = null

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
        if (_vpnState.value == VpnState.CONNECTED) {
            // Reconnect logic
            toggleVpn() // disconnect
            toggleVpn() // connect new
        }
    }

    fun setProtocol(proto: VpnProtocol) {
        _protocol.value = proto
    }

    fun toggleVpn() {
        when (_vpnState.value) {
            VpnState.DISCONNECTED -> {
                startConnecting()
            }
            VpnState.CONNECTED, VpnState.CONNECTING -> {
                disconnect()
            }
            else -> {}
        }
    }

    private fun startConnecting() {
        connectionJob?.cancel()
        _vpnState.value = VpnState.CONNECTING
        _downloadSpeed.value = 0.0f
        _uploadSpeed.value = 0.0f
        
        connectionJob = externalScope.launch(Dispatchers.Default) {
            delay(1800) // Realistic secure handshake
            _vpnState.value = VpnState.CONNECTED
            _assignedIp.value = _selectedServer.value.ipAddress
            startTelemetry()
        }
    }

    private fun disconnect() {
        connectionJob?.cancel()
        telemetryJob?.cancel()
        _vpnState.value = VpnState.DISCONNECTED
        _assignedIp.value = "Original Carrier IP"
        _downloadSpeed.value = 0.0f
        _uploadSpeed.value = 0.0f
        _sessionDurationSeconds.value = 0
    }

    private fun startTelemetry() {
        telemetryJob?.cancel()
        telemetryJob = externalScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000)
                _sessionDurationSeconds.value += 1
                
                // Fluctuating network telemetry speeds
                val down = Random.nextFloat() * 12.5f + 4.2f
                val up = Random.nextFloat() * 4.8f + 1.1f
                _downloadSpeed.value = down
                _uploadSpeed.value = up
                
                // Increment overall telemetry bounds
                val addedMb = (down + up) / 8.0f // MegaBytes transfer
                _bandwidthUsedMb.value += addedMb
            }
        }
    }
}
