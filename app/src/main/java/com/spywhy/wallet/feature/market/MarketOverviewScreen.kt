package com.spywhy.wallet.feature.market

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.domain.model.PriceData
import com.spywhy.wallet.feature.market.viewmodel.MarketTab
import com.spywhy.wallet.feature.market.viewmodel.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketOverviewScreen(
    viewModel: MarketViewModel,
    onCoinClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = MarketTab.entries

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpyWhyColors.Black)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Market",
            style = MaterialTheme.typography.headlineMedium,
            color = SpyWhyColors.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            placeholder = {
                Text(text = "Search coins...", color = SpyWhyColors.TextDisabled)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = SpyWhyColors.TextSecondary
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SpyWhyColors.TextPrimary,
                unfocusedTextColor = SpyWhyColors.TextPrimary,
                cursorColor = SpyWhyColors.White,
                focusedBorderColor = SpyWhyColors.White.copy(alpha = 0.5f),
                unfocusedBorderColor = SpyWhyColors.BorderGray,
                focusedContainerColor = SpyWhyColors.CardBackground,
                unfocusedContainerColor = SpyWhyColors.CardBackground
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = SpyWhyColors.Black,
            contentColor = SpyWhyColors.White,
            edgePadding = 20.dp,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = SpyWhyColors.White
                    )
                }
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        viewModel.selectTab(tab)
                    },
                    text = {
                        Text(
                            text = tab.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTabIndex == index) SpyWhyColors.White
                            else SpyWhyColors.TextSecondary
                        )
                    }
                )
            }
        }

        // Coin list with pull to refresh
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            val displayList = when (tabs[selectedTabIndex]) {
                MarketTab.ALL -> uiState.coinList
                MarketTab.FAVORITES -> uiState.favorites
                MarketTab.TOP_GAINERS -> uiState.topGainers
                MarketTab.TOP_LOSERS -> uiState.topLosers
            }

            if (displayList.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty()) "No coins match your search"
                        else "No data available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SpyWhyColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(
                        items = displayList,
                        key = { _, item -> item.coinId }
                    ) { index, coin ->
                        CoinListItem(
                            rank = index + 1,
                            coin = coin,
                            isFavorite = uiState.favoriteIds.contains(coin.coinId),
                            onFavoriteToggle = { viewModel.toggleFavorite(coin.coinId) },
                            onClick = { onCoinClick(coin.coinId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoinListItem(
    rank: Int,
    coin: PriceData,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    val changeColor = if (coin.change24h >= 0) SpyWhyColors.AccentGreen else SpyWhyColors.AccentRed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Text(
            text = "$rank",
            style = MaterialTheme.typography.bodySmall,
            color = SpyWhyColors.TextSecondary,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )

        // Coin icon placeholder
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SpyWhyColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = coin.coinId.take(2).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = SpyWhyColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name & symbol
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = coin.coinId.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = SpyWhyColors.White,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = coin.coinId.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = SpyWhyColors.TextSecondary
            )
        }

        // Mini sparkline chart
        if (coin.sparkline.isNotEmpty()) {
            MiniSparkline(
                data = coin.sparkline,
                color = changeColor,
                modifier = Modifier
                    .width(56.dp)
                    .height(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Price & change
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatPrice(coin.priceUsd),
                style = MaterialTheme.typography.titleMedium,
                color = SpyWhyColors.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${if (coin.change24h >= 0) "+" else ""}${"%.2f".format(coin.change24h)}%",
                style = MaterialTheme.typography.bodySmall,
                color = changeColor,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Favorite
        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) SpyWhyColors.AccentRed else SpyWhyColors.TextDisabled,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun MiniSparkline(
    data: List<Double>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) return

    val minValue = data.min()
    val maxValue = data.max()
    val range = (maxValue - minValue).coerceAtLeast(0.0001)

    Box(
        modifier = modifier
            .drawBehind {
                val path = Path()
                val widthStep = size.width / (data.size - 1)

                data.forEachIndexed { index, value ->
                    val x = index * widthStep
                    val y = size.height - ((value - minValue) / range * size.height).toFloat()
                    if (index == 0) path.moveTo(x, y)
                    else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 1.5f)
                )
            }
    )
}

private fun formatPrice(price: Double): String {
    return when {
        price >= 1000 -> "$${"%,.0f".format(price)}"
        price >= 1 -> "$${"%,.2f".format(price)}"
        price >= 0.01 -> "$${"%,.4f".format(price)}"
        else -> "$${"%,.8f".format(price)}"
    }
}
