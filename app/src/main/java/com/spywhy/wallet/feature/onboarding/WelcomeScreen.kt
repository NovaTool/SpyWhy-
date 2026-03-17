package com.spywhy.wallet.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spywhy.wallet.R
import com.spywhy.wallet.core.util.SpyWhyColors

@Composable
fun WelcomeScreen(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit,
    onWatchOnly: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // SpyWhy logo
        Image(
            painter = painterResource(id = R.drawable.ic_spywhy_logo),
            contentDescription = "SpyWhy Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // App name
        Text(
            text = "SpyWhy",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            ),
            color = SpyWhyColors.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tagline
        Text(
            text = "Hardware Security Wallet",
            style = MaterialTheme.typography.titleMedium,
            color = SpyWhyColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // Create Wallet Button
        Button(
            onClick = onCreateWallet,
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
                text = "Create Wallet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = SpyWhyColors.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Import Wallet Button
        OutlinedButton(
            onClick = onImportWallet,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SpyWhyColors.TextPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, SpyWhyColors.BorderGray)
        ) {
            Text(
                text = "Import Wallet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = SpyWhyColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Watch-Only Button
        TextButton(
            onClick = onWatchOnly,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Watch-Only",
                style = MaterialTheme.typography.titleMedium,
                color = SpyWhyColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
