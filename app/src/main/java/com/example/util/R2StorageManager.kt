package com.example.util

import android.util.Log
import com.example.data.local.entities.R2StorageConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object R2StorageManager {
    private const val TAG = "R2StorageManager"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Upload a file to the active Cloudflare R2 bucket using AWS SigV4 signed PUT request
     * @return Public URL of uploaded object
     */
    suspend fun uploadFile(
        config: R2StorageConfigEntity,
        file: File,
        mimeType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val extension = if (mimeType.contains("video") || file.name.endsWith(".mp4")) "mp4" else "jpg"
            val objectKey = "media_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$extension"

            val accountId = config.accountId.trim().removePrefix("https://").removeSuffix("/")
            val bucketName = config.bucketName.trim()
            val host = if (accountId.contains("r2.cloudflarestorage.com")) {
                accountId
            } else {
                "$accountId.r2.cloudflarestorage.com"
            }

            val requestUrl = "https://$host/$bucketName/$objectKey"
            val canonicalUri = "/$bucketName/$objectKey"

            // Dates in UTC
            val now = Date()
            val amzDate = getIso8601Date(now)
            val dateOnly = getDateOnly(now)

            // SHA256 of payload
            val fileBytes = file.readBytes()
            val payloadHash = sha256Hex(fileBytes)

            // Canonical Request
            val signedHeaders = "host;x-amz-content-sha256;x-amz-date"
            val canonicalHeaders = "host:$host\nx-amz-content-sha256:$payloadHash\nx-amz-date:$amzDate\n"
            val canonicalRequest = "PUT\n$canonicalUri\n\n$canonicalHeaders\n$signedHeaders\n$payloadHash"
            val canonicalRequestHash = sha256Hex(canonicalRequest.toByteArray(Charsets.UTF_8))

            // String to Sign
            val scope = "$dateOnly/auto/s3/aws4_request"
            val stringToSign = "AWS4-HMAC-SHA256\n$amzDate\n$scope\n$canonicalRequestHash"

            // Key Derivation
            val signingKey = getSignatureKey(config.secretAccessKey.trim(), dateOnly, "auto", "s3")
            val signature = hmacSha256Hex(signingKey, stringToSign)

            val authHeader = "AWS4-HMAC-SHA256 Credential=${config.accessKeyId.trim()}/$scope, SignedHeaders=$signedHeaders, Signature=$signature"

            val mediaTypeObj = mimeType.toMediaTypeOrNull()
            val requestBody = file.asRequestBody(mediaTypeObj)

            val request = Request.Builder()
                .url(requestUrl)
                .put(requestBody)
                .addHeader("Host", host)
                .addHeader("x-amz-date", amzDate)
                .addHeader("x-amz-content-sha256", payloadHash)
                .addHeader("Authorization", authHeader)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful || response.code in 200..204) {
                // Success! Construct public URL
                val publicUrl = if (config.publicEndpoint.isNotBlank()) {
                    val cleanEndpoint = config.publicEndpoint.trim().trimEnd('/')
                    if (cleanEndpoint.startsWith("http://") || cleanEndpoint.startsWith("https://")) {
                        "$cleanEndpoint/$objectKey"
                    } else {
                        "https://$cleanEndpoint/$objectKey"
                    }
                } else {
                    requestUrl
                }
                Log.d(TAG, "R2 Upload successful: $publicUrl")
                Result.success(publicUrl)
            } else {
                val errorBody = response.body?.string() ?: ""
                Log.e(TAG, "R2 Upload failed: code=${response.code}, msg=${response.message}, body=$errorBody")
                Result.failure(Exception("Cloudflare R2 error (${response.code}): ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "R2 Upload exception", e)
            Result.failure(e)
        }
    }

    /**
     * Test connection to Cloudflare R2 bucket with a tiny test object
     */
    suspend fun testConnection(config: R2StorageConfigEntity): Result<String> = withContext(Dispatchers.IO) {
        try {
            val accountId = config.accountId.trim().removePrefix("https://").removeSuffix("/")
            val bucketName = config.bucketName.trim()
            val host = if (accountId.contains("r2.cloudflarestorage.com")) {
                accountId
            } else {
                "$accountId.r2.cloudflarestorage.com"
            }
            val testKey = "test-ping-${System.currentTimeMillis()}.txt"
            val requestUrl = "https://$host/$bucketName/$testKey"
            val canonicalUri = "/$bucketName/$testKey"

            val now = Date()
            val amzDate = getIso8601Date(now)
            val dateOnly = getDateOnly(now)

            val testPayload = "ping test".toByteArray(Charsets.UTF_8)
            val payloadHash = sha256Hex(testPayload)

            val signedHeaders = "host;x-amz-content-sha256;x-amz-date"
            val canonicalHeaders = "host:$host\nx-amz-content-sha256:$payloadHash\nx-amz-date:$amzDate\n"
            val canonicalRequest = "PUT\n$canonicalUri\n\n$canonicalHeaders\n$signedHeaders\n$payloadHash"
            val canonicalRequestHash = sha256Hex(canonicalRequest.toByteArray(Charsets.UTF_8))

            val scope = "$dateOnly/auto/s3/aws4_request"
            val stringToSign = "AWS4-HMAC-SHA256\n$amzDate\n$scope\n$canonicalRequestHash"

            val signingKey = getSignatureKey(config.secretAccessKey.trim(), dateOnly, "auto", "s3")
            val signature = hmacSha256Hex(signingKey, stringToSign)

            val authHeader = "AWS4-HMAC-SHA256 Credential=${config.accessKeyId.trim()}/$scope, SignedHeaders=$signedHeaders, Signature=$signature"

            val request = Request.Builder()
                .url(requestUrl)
                .put(testPayload.toRequestBody("text/plain".toMediaTypeOrNull()))
                .addHeader("Host", host)
                .addHeader("x-amz-date", amzDate)
                .addHeader("x-amz-content-sha256", payloadHash)
                .addHeader("Authorization", authHeader)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful || response.code in 200..204) {
                Result.success("Connection verified! R2 Bucket is active and writable.")
            } else {
                val errorBody = response.body?.string() ?: ""
                Result.failure(Exception("R2 response code ${response.code}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // SigV4 Cryptographic Helpers
    // -------------------------------------------------------------
    private fun getIso8601Date(date: Date): String {
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    private fun getDateOnly(date: Date): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return bytesToHex(digest)
    }

    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    private fun hmacSha256Hex(key: ByteArray, data: String): String {
        return bytesToHex(hmacSha256(key, data))
    }

    private fun getSignatureKey(key: String, dateStamp: String, regionName: String, serviceName: String): ByteArray {
        val kSecret = ("AWS4$key").toByteArray(Charsets.UTF_8)
        val kDate = hmacSha256(kSecret, dateStamp)
        val kRegion = hmacSha256(kDate, regionName)
        val kService = hmacSha256(kRegion, serviceName)
        return hmacSha256(kService, "aws4_request")
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val i = b.toInt() and 0xFF
            result.append(hexChars[i shr 4])
            result.append(hexChars[i and 0x0F])
        }
        return result.toString()
    }
}
