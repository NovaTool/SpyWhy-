package com.spywhy.wallet.domain.model

data class NFT(
    val contractAddress: String,
    val tokenId: String,
    val name: String,
    val description: String?,
    val imageUri: String?,
    val metadata: String?,
    val blockchain: Blockchain
)
