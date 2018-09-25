package org.jetbrains.kotlinconf.api

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.http.*
import kotlinx.io.core.*
import org.jetbrains.kotlinconf.data.*

internal expect val END_POINT: String

class KotlinConfApi {

    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer().apply {
                setMapper(AllData::class, AllData.serializer())
                setMapper(Favorite::class, Favorite.serializer())
                setMapper(Vote::class, Vote.serializer())
            }
        }
        install(ExpectSuccess)
    }

    suspend fun createUser(userId: String): Boolean = client.request<HttpResponse> {
        apiUrl("users")
        method = HttpMethod.Post
        body = userId
    }.use {
        it.status.isSuccess()
    }

    suspend fun getAll(userId: String?): AllData = client.get {
        apiUrl("all", userId)
    }

    suspend fun postFavorite(favorite: Favorite, userId: String): Unit = client.post {
        apiUrl("favorites", userId)
        json()
        body = favorite
    }

    suspend fun deleteFavorite(favorite: Favorite, userId: String): Unit = client.delete {
        apiUrl("favorites", userId)
        json()
        body = favorite
    }

    suspend fun postVote(vote: Vote, userId: String): Unit = client.post {
        apiUrl("votes", userId)
        json()
        body = vote
    }

    suspend fun deleteVote(vote: Vote, userId: String): Unit = client.delete {
        apiUrl("votes", userId)
        json()
        body = vote
    }

    suspend fun verifyCode(code: VotingCode): Unit = client.get {
        apiUrl("users/verify/$code")
    }

    private fun HttpRequestBuilder.json() {
        contentType(ContentType.Application.Json)
    }

    private fun HttpRequestBuilder.apiUrl(path: String, userId: String? = null) {
        if (userId != null) {
            header(HttpHeaders.Authorization, "Bearer $userId")
        }
        header(HttpHeaders.CacheControl, "no-cache")
        url {
            takeFrom(END_POINT)
            encodedPath = path
        }
    }
}
