package com.spywhy.wallet.feature.hwmode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.spywhy.wallet.feature.hwmode.viewmodel.ConnectionMethod
import com.spywhy.wallet.feature.hwmode.viewmodel.HWModeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PCConnectionScreen(
    viewModel: HWModeViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "PC Connection",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SpyWhyColors.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SpyWhyColors.TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SpyWhyColors.Black
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            // Connection status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (uiState.isPCConnected) SpyWhyColors.AccentGreen.copy(alpha = 0.1f)
                        else SpyWhyColors.CardBackground
                    )
                    .border(
                        width = 1.dp,
                        color = if (uiState.isPCConnected) SpyWhyColors.AccentGreen.copy(alpha = 0.3f)
                        else SpyWhyColors.BorderGray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (uiState.isPCConnected) SpyWhyColors.AccentGreen
                            else SpyWhyColors.AccentRed
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (uiState.isPCConnected) "Connected" else "Disconnected",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.isPCConnected) SpyWhyColors.AccentGreen
                        else SpyWhyColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (uiState.isPCConnected && uiState.selectedConnectionMethod != null) {
                        Text(
                            text = "via ${uiState.selectedConnectionMethod!!.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpyWhyColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Connection Method",
                style = MaterialTheme.typography.titleMedium,
                color = SpyWhyColors.White,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select how to connect with the companion PC app",
                style = MaterialTheme.typography.bodySmall,
                color = SpyWhyColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Connection methods
            ConnectionMethodCard(
                icon = Icons.Filled.QrCode,
                title = "QR Relay",
                description = "Air-gapped communication via QR codes. No network required.",
                securityRating = 5,
                isSelected = uiState.selectedConnectionMethod == ConnectionMethod.QR_RELAY,
                badge = "Most Secure",
                onClick = { viewModel.selectConnectionMethod(ConnectionMethod.QR_RELAY) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ConnectionMethodCard(
                icon = Icons.Filled.Usb,
                title = "USB",
                description = "Direct wired connection. Fast and reliable data transfer.",
                securityRating = 4,
                isSelected = uiState.selectedConnectionMethod == ConnectionMethod.USB,
                onClick = { viewModel.selectConnectionMethod(ConnectionMethod.USB) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ConnectionMethodCard(
                icon = Icons.Filled.Wifi,
                title = "WiFi-Direct",
                description = "Peer-to-peer WiFi. No internet access needed.",
                securityRating = 3,
                isSelected = uiState.selectedConnectionMethod == ConnectionMethod.WIFI_DIRECT,
                onClick = { viewModel.selectConnectionMethod(ConnectionMethod.WIFI_DIRECT) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ConnectionMethodCard(
                icon = Icons.Filled.Bluetooth,
                title = "BLE",
                description = "Bluetooth Low Energy. Short range, low power.",
                securityRating = 3,
                isSelected = uiState.selectedConnectionMethod == ConnectionMethod.BLE,
                onClick = { viewModel.selectConnectionMethod(ConnectionMethod.BLE) }
            )

            // Pairing QR for QR relay
            if (uiState.selectedConnectionMethod == ConnectionMethod.QR_RELAY) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Pairing QR Code",
                    style = MaterialTheme.typography.titleMedium,
                    color = SpyWhyColors.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Scan this QR code with the SpyWhy PC companion app",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpyWhyColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SpyWhyColors.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // In production, render actual QR code with pairing data
                    Text(
                        text = "Pairing QR Code",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SpyWhyColors.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ConnectionMethodCard(
    icon: ImageVector,
    title: String,
    description: String,
    securityRating: Int,
    isSelected: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) SpyWhyColors.AccentGreen.copy(alpha = 0.5f)
    else SpyWhyColors.BorderGray
    val bgColor = if (isSelected) SpyWhyColors.AccentGreen.copy(alpha = 0.05f)
    else SpyWhyColors.CardBackground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SpyWhyColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) SpyWhyColors.AccentGreen else SpyWhyColors.TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = SpyWhyColors.White,
                    fontWeight = FontWeight.SemiBold
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = SpyWhyColors.AccentGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SpyWhyColors.AccentGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SpyWhyColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Security rating shields
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = if (index < securityRating) SpyWhyColors.AccentGreen
                        else SpyWhyColors.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$securityRating/5",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpyWhyColors.TextSecondary
                )
            }
        }
    }
}
