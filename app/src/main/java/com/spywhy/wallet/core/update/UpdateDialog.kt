package com.spywhy.wallet.core.update

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spywhy.wallet.core.util.SpyWhyColors

enum class UpdateState {
    READY,
    DOWNLOADING,
    INSTALLING,
    ERROR
}

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    updateState: UpdateState = UpdateState.READY,
    downloadProgress: Float = 0f,
    errorMessage: String? = null,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    val canDismiss = updateState == UpdateState.READY || updateState == UpdateState.ERROR

    Dialog(
        onDismissRequest = { if (canDismiss) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = canDismiss, dismissOnClickOutside = canDismiss)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (updateState) {
                    UpdateState.READY -> ReadyContent(updateInfo, onUpdate, onDismiss)
                    UpdateState.DOWNLOADING -> DownloadingContent(downloadProgress)
                    UpdateState.INSTALLING -> InstallingContent()
                    UpdateState.ERROR -> ErrorContent(errorMessage, onUpdate, onDismiss)
                }
            }
        }
    }
}

@Composable
private fun ReadyContent(
    updateInfo: UpdateInfo,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    Icon(
        imageVector = Icons.Filled.SystemUpdate,
        contentDescription = null,
        tint = SpyWhyColors.AccentOrange,
        modifier = Modifier.size(48.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Mise à jour disponible",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = SpyWhyColors.TextPrimary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Version ${updateInfo.versionName}",
        style = MaterialTheme.typography.bodyLarge,
        color = SpyWhyColors.AccentOrange
    )

    if (updateInfo.changelog.isNotBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = updateInfo.changelog,
            style = MaterialTheme.typography.bodyMedium,
            color = SpyWhyColors.TextSecondary
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SpyWhyColors.TextSecondary)
        ) {
            Text("Plus tard")
        }

        Button(
            onClick = onUpdate,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = SpyWhyColors.AccentOrange)
        ) {
            Text("Installer", color = SpyWhyColors.Black)
        }
    }
}

@Composable
private fun DownloadingContent(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "download")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Icon(
        imageVector = Icons.Filled.CloudDownload,
        contentDescription = null,
        tint = SpyWhyColors.AccentOrange,
        modifier = Modifier
            .size(48.dp)
            .rotate(if (progress <= 0f) rotation else 0f)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Téléchargement en cours...",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = SpyWhyColors.TextPrimary
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (progress > 0f) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = SpyWhyColors.AccentOrange,
            trackColor = SpyWhyColors.DarkGray,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${(progress * 100).toInt()}%",
            color = SpyWhyColors.AccentOrange,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    } else {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = SpyWhyColors.AccentOrange,
            trackColor = SpyWhyColors.DarkGray,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Préparation...",
            color = SpyWhyColors.TextSecondary,
            fontSize = 13.sp
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Ne fermez pas l'application",
        color = SpyWhyColors.TextDisabled,
        fontSize = 12.sp
    )
}

@Composable
private fun InstallingContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "install")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Icon(
        imageVector = Icons.Filled.InstallMobile,
        contentDescription = null,
        tint = SpyWhyColors.AccentGreen,
        modifier = Modifier.size((48 * scale).dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Installation...",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = SpyWhyColors.TextPrimary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "L'installateur Android va s'ouvrir",
        color = SpyWhyColors.TextSecondary,
        fontSize = 13.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ErrorContent(
    errorMessage: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Icon(
        imageVector = Icons.Filled.Error,
        contentDescription = null,
        tint = SpyWhyColors.AccentRed,
        modifier = Modifier.size(48.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Erreur de téléchargement",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = SpyWhyColors.AccentRed
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = errorMessage ?: "Le téléchargement a échoué. Vérifiez votre connexion.",
        color = SpyWhyColors.TextSecondary,
        fontSize = 13.sp,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SpyWhyColors.TextSecondary)
        ) {
            Text("Fermer")
        }

        Button(
            onClick = onRetry,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = SpyWhyColors.AccentOrange)
        ) {
            Text("Réessayer", color = SpyWhyColors.Black)
        }
    }
}
