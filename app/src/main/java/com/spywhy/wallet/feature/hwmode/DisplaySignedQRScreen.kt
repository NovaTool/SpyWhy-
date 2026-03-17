package com.spywhy.wallet.feature.hwmode

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.feature.hwmode.viewmodel.HWModeViewModel
import kotlinx.coroutines.delay

@Composable
fun DisplaySignedQRScreen(
    viewModel: HWModeViewModel,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val qrFrames = uiState.signedQRFrames
    val isMultiFrame = qrFrames.size > 1

    var currentFrameIndex by remember { mutableIntStateOf(0) }

    // Animate through frames for multi-frame QR
    if (isMultiFrame) {
        LaunchedEffect(qrFrames.size) {
            while (true) {
                delay(500L) // 500ms per frame
                currentFrameIndex = (currentFrameIndex + 1) % qrFrames.size
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Success icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(SpyWhyColors.AccentGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Signed",
                tint = SpyWhyColors.AccentGreen,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Transaction Signed",
            style = MaterialTheme.typography.headlineMedium,
            color = SpyWhyColors.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Show this QR code to your PC to broadcast",
            style = MaterialTheme.typography.bodyMedium,
            color = SpyWhyColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // QR code display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(SpyWhyColors.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (qrFrames.isNotEmpty()) {
                val currentData = qrFrames[currentFrameIndex.coerceIn(0, qrFrames.size - 1)]
                AnimatedContent(
                    targetState = currentFrameIndex,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "qrFrame"
                ) { frameIndex ->
                    // In production, render actual QR using a QR library
                    // e.g., ZXing BarcodeEncoder or compose-qr-code
                    QRCodePlaceholder(
                        data = qrFrames[frameIndex.coerceIn(0, qrFrames.size - 1)],
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Text(
                    text = "No signed transaction data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpyWhyColors.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Multi-frame progress indicator
        if (isMultiFrame) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Frame ${currentFrameIndex + 1} / ${qrFrames.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = SpyWhyColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (currentFrameIndex + 1).toFloat() / qrFrames.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = SpyWhyColors.AccentGreen,
                    trackColor = SpyWhyColors.LightGray
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Frame dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    qrFrames.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (index == currentFrameIndex) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentFrameIndex) SpyWhyColors.AccentGreen
                                    else SpyWhyColors.LightGray
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Warning message
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SpyWhyColors.AccentOrange.copy(alpha = 0.1f))
                .border(
                    width = 1.dp,
                    color = SpyWhyColors.AccentOrange.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Warning",
                tint = SpyWhyColors.AccentOrange,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Scan this QR on your PC to broadcast the transaction.",
                style = MaterialTheme.typography.bodySmall,
                color = SpyWhyColors.AccentOrange
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Done button
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpyWhyColors.White,
                contentColor = SpyWhyColors.Black
            )
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun QRCodePlaceholder(
    data: String,
    modifier: Modifier = Modifier
) {
    // In production, use a QR code generation library:
    // val bitmap = BarcodeEncoder().encodeBitmap(data, BarcodeFormat.QR_CODE, 512, 512)
    // Image(bitmap = bitmap.asImageBitmap(), ...)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SpyWhyColors.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "QR Code\n(${data.take(20)}...)",
            style = MaterialTheme.typography.bodySmall,
            color = SpyWhyColors.Black,
            textAlign = TextAlign.Center
        )
    }
}
