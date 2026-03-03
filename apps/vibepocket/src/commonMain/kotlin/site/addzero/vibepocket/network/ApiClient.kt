package site.addzero.vibepocket.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun searchMusic(query: String): SearchResult { // Assuming SearchResult is a serializable class
        return client.get("/api/music/search") {
            parameter("query", query)
        }.body()
    }

    suspend fun generateSong(prompt: String): GenerationResult { // Assuming GenerationResult is a serializable class
        return client.post("/api/suno/generate") {
            parameter("prompt", prompt)
        }.body()
    }
}

// I need to define SearchResult and GenerationResult based on what the backend sends.
// For now, I'll create placeholder data classes.

@kotlinx.serialization.Serializable
data class SearchResult(val results: List<Song>)

@kotlinx.serialization.Serializable
data class Song(val title: String, val artist: String, val url: String)

@kotlinx.serialization.Serializable
data class GenerationResult(val songUrl: String)
