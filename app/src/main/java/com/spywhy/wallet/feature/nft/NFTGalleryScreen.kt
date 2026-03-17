package com.spywhy.wallet.feature.nft

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spywhy.wallet.core.util.SpyWhyColors
import com.spywhy.wallet.feature.nft.viewmodel.NFTViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFTGalleryScreen(
    onNavigateBack: () -> Unit,
    viewModel: NFTViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NFT Gallery", color = SpyWhyColors.TextPrimary)
                        Text("${state.nfts.size} items", color = SpyWhyColors.TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = SpyWhyColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpyWhyColors.Black)
            )
        },
        containerColor = SpyWhyColors.Black
    ) { padding ->
        if (state.nfts.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Collections,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = SpyWhyColors.TextDisabled
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No NFTs found", color = SpyWhyColors.TextSecondary)
                    Text("NFTs from your wallets will appear here", color = SpyWhyColors.TextDisabled, fontSize = 12.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(state.nfts) { nft ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectNFT(nft) },
                        colors = CardDefaults.cardColors(containerColor = SpyWhyColors.MediumGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            // NFT Image placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                    .background(SpyWhyColors.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = SpyWhyColors.TextDisabled
                                )
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    nft.name,
                                    color = SpyWhyColors.TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "#${nft.tokenId}",
                                    color = SpyWhyColors.TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    nft.blockchain.name,
                                    color = SpyWhyColors.AccentGreen,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SpyWhyColors.AccentGreen)
            }
        }
    }
}
