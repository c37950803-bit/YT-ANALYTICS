package com.example.data.remote.api

import com.example.data.remote.model.YouTubeChannelListResponse
import com.example.data.remote.model.YouTubePlaylistItemListResponse
import com.example.data.remote.model.YouTubeVideoListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface Retrofit déclarant les points d'entrée officiels de l'API YouTube Data v3.
 *
 * Explication technique :
 * Chaque appel nécessite le paramètre `key` (la clé API fournie par l'utilisateur).
 * Nous utilisons des `suspend` functions pour s'intégrer naturellement avec les Coroutines Kotlin.
 */
interface YouTubeApiService {

    /**
     * Recherche les informations détaillées d'une chaîne par son identifiant unique (Channel ID).
     *
     * @param part Composants demandés ("snippet,statistics,contentDetails,brandingSettings")
     * @param channelId L'ID unique (ex: "UCX6OQ3DkcsbYNE6H8uQQuVA")
     * @param apiKey La clé API YouTube active
     */
    @GET("youtube/v3/channels")
    suspend fun getChannelById(
        @Query("part") part: String = "snippet,statistics,contentDetails,brandingSettings",
        @Query("id") channelId: String,
        @Query("key") apiKey: String
    ): Response<YouTubeChannelListResponse>

    /**
     * Recherche les informations détaillées d'une chaîne par son Handle (ex: "@MrBeast").
     *
     * @param part Composants demandés
     * @param handle Le handle (avec ou sans '@')
     * @param apiKey La clé API YouTube active
     */
    @GET("youtube/v3/channels")
    suspend fun getChannelByHandle(
        @Query("part") part: String = "snippet,statistics,contentDetails,brandingSettings",
        @Query("forHandle") handle: String,
        @Query("key") apiKey: String
    ): Response<YouTubeChannelListResponse>

    /**
     * Recherche les informations d'une chaîne par son ancien nom d'utilisateur (Username legacy).
     */
    @GET("youtube/v3/channels")
    suspend fun getChannelByUsername(
        @Query("part") part: String = "snippet,statistics,contentDetails,brandingSettings",
        @Query("forUsername") username: String,
        @Query("key") apiKey: String
    ): Response<YouTubeChannelListResponse>

    /**
     * Récupère la liste des vidéos mises en ligne par la chaîne via sa playlist "Uploads".
     *
     * Explication métier :
     * L'API YouTube ne permet pas de lister les vidéos d'une chaîne directement à bas coût;
     * la méthode optimale et officielle (1 unité de quota au lieu de 100 pour search)
     * consiste à interroger la playlist d'Uploads (`UU...`) de la chaîne.
     *
     * @param playlistId L'ID de la playlist uploads (généralement "UU" + channelId sans "UC")
     * @param maxResults Nombre max d'éléments (jusqu'à 50)
     * @param apiKey Clé API active
     */
    @GET("youtube/v3/playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("key") apiKey: String
    ): Response<YouTubePlaylistItemListResponse>

    /**
     * Récupère les statistiques détaillées (vues, likes, commentaires) pour une liste d'IDs de vidéos.
     *
     * @param part Composants demandés ("snippet,statistics,contentDetails")
     * @param videoIds IDs séparés par des virgules (ex: "id1,id2,id3")
     * @param apiKey Clé API active
     */
    @GET("youtube/v3/videos")
    suspend fun getVideosDetails(
        @Query("part") part: String = "snippet,statistics,contentDetails",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String
    ): Response<YouTubeVideoListResponse>
}
