package com.spywhy.wallet.feature.hwmode

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.feature.hwmode.viewmodel.HWModeViewModel

@Composable
fun ScanTxQRScreen(
    viewModel: HWModeViewModel,
    onCancel: () -> Unit,
    onScanned: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scanProgress = uiState.scanProgress
    val totalFrames = uiState.totalQRFrames
    val scannedFrames = uiState.scannedQRFrames

    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanLinePosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        // Camera preview placeholder
        // In production, this would use CameraX PreviewView via AndroidView
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Camera preview area
            CameraPreviewPlaceholder(
                modifier = Modifier.fillMaxSize()
            )
        }

        // QR Scanner overlay
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val scanAreaSize = screenWidth * 0.7f

        Box(modifier = Modifier.fillMaxSize()) {
            // Darkened overlay with transparent center
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val scanSize = canvasWidth * 0.7f
                val left = (canvasWidth - scanSize) / 2f
                val top = (canvasHeight - scanSize) / 2f - 60f

                // Dark overlay
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    size = size
                )

                // Clear center area
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(left, top),
                    size = Size(scanSize, scanSize),
                    cornerRadius = CornerRadius(24f, 24f),
                    blendMode = BlendMode.Clear
                )

                // Scan area border
                drawRoundRect(
                    color = SpyWhyColors.AccentGreen,
                    topLeft = Offset(left, top),
                    size = Size(scanSize, scanSize),
                    cornerRadius = CornerRadius(24f, 24f),
                    style = Stroke(width = 3f)
                )

                // Corner accents
                val cornerLen = 40f
                val cornerStroke = 6f
                val cornerColor = SpyWhyColors.AccentGreen

                // Top-left corner
                drawLine(cornerColor, Offset(left, top + cornerLen), Offset(left, top), strokeWidth = cornerStroke)
                drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeWidth = cornerStroke)
                // Top-right corner
                drawLine(cornerColor, Offset(left + scanSize - cornerLen, top), Offset(left + scanSize, top), strokeWidth = cornerStroke)
                drawLine(cornerColor, Offset(left + scanSize, top), Offset(left + scanSize, top + cornerLen), strokeWidth = cornerStroke)
                // Bottom-left corner
                drawLine(cornerColor, Offset(left, top + scanSize - cornerLen), Offset(left, top + scanSize), strokeWidth = cornerStroke)
                drawLine(cornerColor, Offset(left, top + scanSize), Offset(left + cornerLen, top + scanSize), strokeWidth = cornerStroke)
                // Bottom-right corner
                drawLine(cornerColor, Offset(left + scanSize - cornerLen, top + scanSize), Offset(left + scanSize, top + scanSize), strokeWidth = cornerStroke)
                drawLine(cornerColor, Offset(left + scanSize, top + scanSize - cornerLen), Offset(left + scanSize, top + scanSize), strokeWidth = cornerStroke)

                // Animated scan line
                val scanLineY = top + (scanSize * scanLinePosition)
                drawLine(
                    color = SpyWhyColors.AccentGreen.copy(alpha = 0.7f),
                    start = Offset(left + 16f, scanLineY),
                    end = Offset(left + scanSize - 16f, scanLineY),
                    strokeWidth = 2f
                )
            }
        }

        // Top area - title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scan Transaction QR",
                style = MaterialTheme.typography.headlineSmall,
                color = SpyWhyColors.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Point camera at the QR code on your PC",
                style = MaterialTheme.typography.bodyMedium,
                color = SpyWhyColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        // Bottom area - progress and cancel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Multi-frame progress
            if (totalFrames > 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpyWhyColors.CardBackground.copy(alpha = 0.9f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Animated QR: Frame $scannedFrames / $totalFrames",
                        style = MaterialTheme.typography.labelLarge,
                        color = SpyWhyColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { scanProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SpyWhyColors.AccentGreen,
                        trackColor = SpyWhyColors.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(scanProgress * 100).toInt()}% complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpyWhyColors.TextSecondary
                    )
                }
            }

            // Cancel button
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpyWhyColors.AccentRed.copy(alpha = 0.2f),
                    contentColor = SpyWhyColors.AccentRed
                )
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewPlaceholder(modifier: Modifier = Modifier) {
    // In production, replace with:
    // AndroidView(factory = { context ->
    //     PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE }
    // }, modifier = modifier)
    Box(
        modifier = modifier.background(SpyWhyColors.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CameraX Preview",
            style = MaterialTheme.typography.bodyMedium,
            color = SpyWhyColors.TextDisabled
        )
    }
}
