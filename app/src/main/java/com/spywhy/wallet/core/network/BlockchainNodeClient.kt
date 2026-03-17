package com.spywhy.wallet.core.network

import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.core.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainNodeClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val customNodes = mutableMapOf<Blockchain, String>()

    fun setCustomNode(blockchain: Blockchain, url: String) {
        customNodes[blockchain] = url
    }

    fun getNodeUrl(blockchain: Blockchain): String {
        return customNodes[blockchain] ?: when (blockchain) {
            Blockchain.BITCOIN -> Constants.BTC_NODE_DEFAULT
            Blockchain.ETHEREUM -> Constants.ETH_NODE_DEFAULT
            Blockchain.SOLANA -> Constants.SOL_NODE_DEFAULT
            Blockchain.LITECOIN -> Constants.LTC_NODE_DEFAULT
            Blockchain.MONERO -> "http://node.moneroworld.com:18089"
        }
    }

    suspend fun getBalance(blockchain: Blockchain, address: String): String = withContext(Dispatchers.IO) {
        when (blockchain) {
            Blockchain.ETHEREUM -> {
                val json = JSONObject().apply {
                    put("jsonrpc", "2.0")
                    put("method", "eth_getBalance")
                    put("params", org.json.JSONArray().apply {
                        put(address)
                        put("latest")
                    })
                    put("id", 1)
                }
                jsonRpcCall(getNodeUrl(blockchain), json.toString())
            }
            Blockchain.SOLANA -> {
                val json = JSONObject().apply {
                    put("jsonrpc", "2.0")
                    put("method", "getBalance")
                    put("params", org.json.JSONArray().apply { put(address) })
                    put("id", 1)
                }
                jsonRpcCall(getNodeUrl(blockchain), json.toString())
            }
            else -> {
                // For BTC/LTC, use Electrum protocol or block explorer API
                "0"
            }
        }
    }

    suspend fun broadcastTransaction(blockchain: Blockchain, rawTxHex: String): String = withContext(Dispatchers.IO) {
        when (blockchain) {
            Blockchain.ETHEREUM -> {
                val json = JSONObject().apply {
                    put("jsonrpc", "2.0")
                    put("method", "eth_sendRawTransaction")
                    put("params", org.json.JSONArray().apply { put(rawTxHex) })
                    put("id", 1)
                }
                jsonRpcCall(getNodeUrl(blockchain), json.toString())
            }
            Blockchain.SOLANA -> {
                val json = JSONObject().apply {
                    put("jsonrpc", "2.0")
                    put("method", "sendTransaction")
                    put("params", org.json.JSONArray().apply { put(rawTxHex) })
                    put("id", 1)
                }
                jsonRpcCall(getNodeUrl(blockchain), json.toString())
            }
            else -> {
                // BTC/LTC broadcast via Electrum or API
                ""
            }
        }
    }

    suspend fun getTransactionStatus(blockchain: Blockchain, txHash: String): String = withContext(Dispatchers.IO) {
        when (blockchain) {
            Blockchain.ETHEREUM -> {
                val json = JSONObject().apply {
                    put("jsonrpc", "2.0")
                    put("method", "eth_getTransactionReceipt")
                    put("params", org.json.JSONArray().apply { put(txHash) })
                    put("id", 1)
                }
                jsonRpcCall(getNodeUrl(blockchain), json.toString())
            }
            else -> ""
        }
    }

    suspend fun estimateGas(blockchain: Blockchain, from: String, to: String, value: String): String = withContext(Dispatchers.IO) {
        when (blockchain) {
            Blockchain.ETHEREUM -> {
                val txObj = JSONObject().apply {
                    put("from", from)
                    put("to", to)
                    put("value", value)
                }
                val json = JSONObject().apply {
                    put("jsonrpc", "2.0")
                    put("method", "eth_estimateGas")
                    put("params", org.json.JSONArray().apply { put(txObj) })
                    put("id", 1)
                }
                jsonRpcCall(getNodeUrl(blockchain), json.toString())
            }
            else -> "21000"
        }
    }

    private suspend fun jsonRpcCall(url: String, body: String): String = withContext(Dispatchers.IO) {
        val mediaType = "application/json".toMediaType()
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody(mediaType))
            .build()
        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        val jsonResponse = JSONObject(responseBody)
        jsonResponse.optString("result", "")
    }
}
