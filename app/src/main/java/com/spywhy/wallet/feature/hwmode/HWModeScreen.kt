package com.spywhy.wallet.feature.hwmode

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.feature.hwmode.viewmodel.ConnectionMode
import com.spywhy.wallet.feature.hwmode.viewmodel.HWModeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HWModeScreen(
    viewModel: HWModeViewModel,
    onSignTransaction: () -> Unit,
    onShareAddress: () -> Unit,
    onPCConnection: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Shield icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SpyWhyColors.CardBackground)
                .border(
                    width = 2.dp,
                    color = SpyWhyColors.AccentGreen.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = "Hardware Wallet",
                tint = SpyWhyColors.AccentGreen,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hardware Wallet Mode",
            style = MaterialTheme.typography.headlineMedium,
            color = SpyWhyColors.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Air-gapped transaction signing",
            style = MaterialTheme.typography.bodyMedium,
            color = SpyWhyColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Status indicator
        StatusIndicator(
            mode = uiState.connectionMode,
            onModeChange = { viewModel.setConnectionMode(it) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action cards
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HWActionCard(
                icon = Icons.Filled.QrCodeScanner,
                title = "Sign Transaction",
                description = "Scan QR from PC, sign offline, show signed QR",
                onClick = onSignTransaction
            )

            HWActionCard(
                icon = Icons.Filled.Share,
                title = "Share Address",
                description = "Display address QR for PC to scan",
                onClick = onShareAddress
            )

            HWActionCard(
                icon = Icons.Filled.CastConnected,
                title = "PC Connection",
                description = "Setup secure connection with companion app",
                onClick = onPCConnection
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusIndicator(
    mode: ConnectionMode,
    onModeChange: (ConnectionMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val statusColor by animateColorAsState(
                targetValue = when (mode) {
                    ConnectionMode.ONLINE -> SpyWhyColors.AccentGreen
                    ConnectionMode.OFFLINE -> SpyWhyColors.AccentOrange
                    ConnectionMode.AIR_GAPPED -> SpyWhyColors.AccentRed
                },
                label = "statusColor"
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (mode) {
                    ConnectionMode.ONLINE -> "Online"
                    ConnectionMode.OFFLINE -> "Offline"
                    ConnectionMode.AIR_GAPPED -> "Air-Gapped"
                },
                style = MaterialTheme.typography.labelLarge,
                color = SpyWhyColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            ConnectionMode.entries.forEachIndexed { index, connectionMode ->
                SegmentedButton(
                    selected = mode == connectionMode,
                    onClick = { onModeChange(connectionMode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ConnectionMode.entries.size
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = SpyWhyColors.LightGray,
                        activeContentColor = SpyWhyColors.White,
                        inactiveContainerColor = SpyWhyColors.CardBackground,
                        inactiveContentColor = SpyWhyColors.TextSecondary,
                        activeBorderColor = SpyWhyColors.BorderGray,
                        inactiveBorderColor = SpyWhyColors.BorderGray
                    )
                ) {
                    Text(
                        text = when (connectionMode) {
                            ConnectionMode.ONLINE -> "Online"
                            ConnectionMode.OFFLINE -> "Offline"
                            ConnectionMode.AIR_GAPPED -> "Air-Gapped"
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun HWActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SpyWhyColors.CardBackground)
            .border(
                width = 1.dp,
                color = SpyWhyColors.BorderGray,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SpyWhyColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = SpyWhyColors.TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SpyWhyColors.White,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SpyWhyColors.TextSecondary
            )
        }
    }
}
