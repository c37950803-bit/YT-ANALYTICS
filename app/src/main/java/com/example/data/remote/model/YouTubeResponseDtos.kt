package com.example.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modèles DTO (Data Transfer Objects) pour l'API YouTube Data v3.
 *
 * Explication technique :
 * Ces classes reflètent exactement la structure JSON retournée par les endpoints
 * de Google YouTube API v3.
 */

// ==========================================
// 1. DTOs pour /channels
// ==========================================

@JsonClass(generateAdapter = true)
data class YouTubeChannelListResponse(
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "items") val items: List<YouTubeChannelItem>? = null,
    @Json(name = "pageInfo") val pageInfo: PageInfoDto? = null,
    @Json(name = "error") val error: YouTubeErrorDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelItem(
    @Json(name = "id") val id: String,
    @Json(name = "snippet") val snippet: ChannelSnippetDto? = null,
    @Json(name = "contentDetails") val contentDetails: ChannelContentDetailsDto? = null,
    @Json(name = "statistics") val statistics: ChannelStatisticsDto? = null,
    @Json(name = "brandingSettings") val brandingSettings: ChannelBrandingSettingsDto? = null
)

@JsonClass(generateAdapter = true)
data class ChannelSnippetDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "customUrl") val customUrl: String? = null,
    @Json(name = "publishedAt") val publishedAt: String? = null,
    @Json(name = "thumbnails") val thumbnails: ThumbnailsContainerDto? = null,
    @Json(name = "country") val country: String? = null
)

@JsonClass(generateAdapter = true)
data class ChannelContentDetailsDto(
    @Json(name = "relatedPlaylists") val relatedPlaylists: RelatedPlaylistsDto? = null
)

@JsonClass(generateAdapter = true)
data class RelatedPlaylistsDto(
    @Json(name = "likes") val likes: String? = null,
    @Json(name = "uploads") val uploads: String? = null
)

@JsonClass(generateAdapter = true)
data class ChannelStatisticsDto(
    @Json(name = "viewCount") val viewCount: String? = null,
    @Json(name = "subscriberCount") val subscriberCount: String? = null,
    @Json(name = "hiddenSubscriberCount") val hiddenSubscriberCount: Boolean? = null,
    @Json(name = "videoCount") val videoCount: String? = null
)

@JsonClass(generateAdapter = true)
data class ChannelBrandingSettingsDto(
    @Json(name = "image") val image: BrandingImageDto? = null
)

@JsonClass(generateAdapter = true)
data class BrandingImageDto(
    @Json(name = "bannerExternalUrl") val bannerExternalUrl: String? = null
)

// ==========================================
// 2. DTOs pour /playlistItems (Uploads playlist)
// ==========================================

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemListResponse(
    @Json(name = "items") val items: List<PlaylistItemDto>? = null,
    @Json(name = "nextPageToken") val nextPageToken: String? = null,
    @Json(name = "pageInfo") val pageInfo: PageInfoDto? = null,
    @Json(name = "error") val error: YouTubeErrorDto? = null
)

@JsonClass(generateAdapter = true)
data class PlaylistItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "snippet") val snippet: PlaylistItemSnippetDto? = null,
    @Json(name = "contentDetails") val contentDetails: PlaylistItemContentDetailsDto? = null
)

@JsonClass(generateAdapter = true)
data class PlaylistItemSnippetDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "publishedAt") val publishedAt: String? = null,
    @Json(name = "thumbnails") val thumbnails: ThumbnailsContainerDto? = null,
    @Json(name = "resourceId") val resourceId: ResourceIdDto? = null
)

@JsonClass(generateAdapter = true)
data class ResourceIdDto(
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "videoId") val videoId: String? = null
)

@JsonClass(generateAdapter = true)
data class PlaylistItemContentDetailsDto(
    @Json(name = "videoId") val videoId: String? = null,
    @Json(name = "videoPublishedAt") val videoPublishedAt: String? = null
)

// ==========================================
// 3. DTOs pour /videos (Stats détaillées)
// ==========================================

@JsonClass(generateAdapter = true)
data class YouTubeVideoListResponse(
    @Json(name = "items") val items: List<YouTubeVideoItemDto>? = null,
    @Json(name = "pageInfo") val pageInfo: PageInfoDto? = null,
    @Json(name = "error") val error: YouTubeErrorDto? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "snippet") val snippet: VideoSnippetDto? = null,
    @Json(name = "contentDetails") val contentDetails: VideoContentDetailsDto? = null,
    @Json(name = "statistics") val statistics: VideoStatisticsDto? = null
)

@JsonClass(generateAdapter = true)
data class VideoSnippetDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "publishedAt") val publishedAt: String? = null,
    @Json(name = "channelId") val channelId: String? = null,
    @Json(name = "channelTitle") val channelTitle: String? = null,
    @Json(name = "thumbnails") val thumbnails: ThumbnailsContainerDto? = null
)

@JsonClass(generateAdapter = true)
data class VideoContentDetailsDto(
    @Json(name = "duration") val duration: String? = null
)

@JsonClass(generateAdapter = true)
data class VideoStatisticsDto(
    @Json(name = "viewCount") val viewCount: String? = null,
    @Json(name = "likeCount") val likeCount: String? = null,
    @Json(name = "commentCount") val commentCount: String? = null
)

// ==========================================
// 4. Modèles communs (Thumbnails, Pagination, Erreurs)
// ==========================================

@JsonClass(generateAdapter = true)
data class ThumbnailsContainerDto(
    @Json(name = "default") val defaultThumb: ThumbnailDto? = null,
    @Json(name = "medium") val mediumThumb: ThumbnailDto? = null,
    @Json(name = "high") val highThumb: ThumbnailDto? = null,
    @Json(name = "standard") val standardThumb: ThumbnailDto? = null,
    @Json(name = "maxres") val maxresThumb: ThumbnailDto? = null
) {
    /**
     * Retourne la meilleure URL d'image disponible.
     */
    fun getBestUrl(): String {
        return maxresThumb?.url
            ?: standardThumb?.url
            ?: highThumb?.url
            ?: mediumThumb?.url
            ?: defaultThumb?.url
            ?: ""
    }
}

@JsonClass(generateAdapter = true)
data class ThumbnailDto(
    @Json(name = "url") val url: String? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null
)

@JsonClass(generateAdapter = true)
data class PageInfoDto(
    @Json(name = "totalResults") val totalResults: Int? = null,
    @Json(name = "resultsPerPage") val resultsPerPage: Int? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeErrorDto(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "errors") val errors: List<YouTubeErrorItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeErrorItemDto(
    @Json(name = "message") val message: String? = null,
    @Json(name = "domain") val domain: String? = null,
    @Json(name = "reason") val reason: String? = null
)
