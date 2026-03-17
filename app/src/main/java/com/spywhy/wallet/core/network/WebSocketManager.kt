package com.spywhy.wallet.core.network

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private var webSocket: WebSocket? = null

    private val _messages = MutableSharedFlow<WebSocketMessage>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages: SharedFlow<WebSocketMessage> = _messages

    private val _connectionState = MutableSharedFlow<ConnectionState>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val connectionState: SharedFlow<ConnectionState> = _connectionState

    fun connect(url: String) {
        disconnect()
        val request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.tryEmit(ConnectionState.CONNECTED)
                Timber.d("WebSocket connected: $url")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _messages.tryEmit(WebSocketMessage(text))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.tryEmit(ConnectionState.DISCONNECTING)
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.tryEmit(ConnectionState.DISCONNECTED)
                Timber.d("WebSocket closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.tryEmit(ConnectionState.ERROR)
                Timber.e(t, "WebSocket error")
            }
        })
        _connectionState.tryEmit(ConnectionState.CONNECTING)
    }

    fun send(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _connectionState.tryEmit(ConnectionState.DISCONNECTED)
    }

    fun subscribeToAddress(address: String) {
        val subscribeMsg = """{"method":"blockchain.scripthash.subscribe","params":["$address"],"id":1}"""
        send(subscribeMsg)
    }

    fun subscribeToMempool() {
        val subscribeMsg = """{"method":"mempool.subscribe","params":[],"id":2}"""
        send(subscribeMsg)
    }
}

data class WebSocketMessage(val data: String)

enum class ConnectionState {
    CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED, ERROR
}
