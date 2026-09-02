package com.yourssu.soongsil.data

import com.yourssu.data.dashboard.AdvertisementData
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

// 인앱 홍보 광고 배너 API를 조회하는 레포지토리입니다.
@Singleton
class AdvertisementRepository @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    // 광고 배너 정보를 불러옵니다.
    suspend fun getAdvertisement(): Result<AdvertisementData?> = runCatching {
        val connection = URL(ADVERTISEMENT_URL).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS

            if (connection.responseCode !in 200..299) {
                error("광고 정보를 불러오지 못했습니다.")
            }

            val advertisement = connection.inputStream.bufferedReader().use { reader ->
                json.decodeFromString<AdvertisementData>(reader.readText())
            }
            advertisement.takeIf {
                it.success && it.imageUrl.isNotBlank() && it.link.isNotBlank()
            }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val ADVERTISEMENT_URL = "https://3.39.172.201/v1/api/ads"
        const val TIMEOUT_MILLIS = 10_000
    }
}
