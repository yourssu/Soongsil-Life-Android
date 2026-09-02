package com.yourssu.soongsil.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

// 기기의 인터넷 연결 상태를 실시간으로 모니터링하는 클래스입니다.
// @param context 안드로이드 애플리케이션 컨텍스트입니다.
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 시스템 ConnectivityManager 서비스 인스턴스입니다.
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    // 현재 기기가 인터넷에 정상적으로 연결되어 있는지 여부를 확인합니다.
    val isCurrentlyConnected: Boolean
        get() {
            val cm = connectivityManager ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

    // 인터넷 연결 상태 변경 흐름을 방출하는 Flow입니다.
    val isOnline: Flow<Boolean> = callbackFlow {
        // 초기 연결 상태를 전달합니다.
        trySend(isCurrentlyConnected)

        // 네트워크 상태 변화를 감지하는 콜백 리스너입니다.
        val callback = object : ConnectivityManager.NetworkCallback() {
            // 네트워크가 연결되었을 때 호출됩니다.
            override fun onAvailable(network: Network) {
                trySend(isCurrentlyConnected)
            }

            // 네트워크 연결이 끊어졌을 때 호출됩니다.
            override fun onLost(network: Network) {
                trySend(isCurrentlyConnected)
            }

            // 네트워크 기능(Capability)이 변경되었을 때 호출됩니다.
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                trySend(hasInternet)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(request, callback)

        // Flow 수집이 중단되면 네트워크 콜백 등록을 해제합니다.
        awaitClose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged().conflate()
}
