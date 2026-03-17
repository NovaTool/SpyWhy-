package com.spywhy.wallet.domain.model

data class Wallet(
    val id: Long,
    val name: String,
    val type: WalletType,
    val blockchains: List<Blockchain>,
    val isHidden: Boolean,
    val createdAt: Long
)

enum class WalletType {
    HD,
    IMPORTED,
    WATCH_ONLY
}
