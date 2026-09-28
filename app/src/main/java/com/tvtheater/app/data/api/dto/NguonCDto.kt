package com.tvtheater.app.data.api.dto

import com.google.gson.annotations.SerializedName

data class FilmListResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("paginate") val paginate: PaginationDto = PaginationDto(),
    @SerializedName("items") val items: List<MovieSummaryDto> = emptyList()
)

data class PaginationDto(
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("total_page") val totalPage: Int = 1,
    @SerializedName("total_items") val totalItems: Int = 0,
    @SerializedName("items_per_page") val itemsPerPage: Int = 10
)

data class MovieSummaryDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("slug") val slug: String = "",
    @SerializedName("original_name") val originalName: String? = null,
    @SerializedName("thumb_url") val thumbUrl: String? = null,
    @SerializedName("poster_url") val posterUrl: String? = null,
    @SerializedName("created") val created: String? = null,
    @SerializedName("modified") val modified: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("total_episodes") val totalEpisodes: Int = 0,
    @SerializedName("current_episode") val currentEpisode: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("director") val director: String? = null,
    @SerializedName("casts") val casts: String? = null,
    @SerializedName("year") val year: String? = null
)

data class FilmDetailResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("movie") val movie: MovieDetailDto = MovieDetailDto()
)

data class MovieDetailDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("slug") val slug: String = "",
    @SerializedName("original_name") val originalName: String? = null,
    @SerializedName("thumb_url") val thumbUrl: String? = null,
    @SerializedName("poster_url") val posterUrl: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("total_episodes") val totalEpisodes: Int = 0,
    @SerializedName("current_episode") val currentEpisode: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("director") val director: String? = null,
    @SerializedName("casts") val casts: String? = null,
    @SerializedName("year") val year: String? = null,
    @SerializedName("episodes") val episodes: List<ServerEpisodeDto> = emptyList()
)

data class ServerEpisodeDto(
    @SerializedName("server_name") val serverName: String = "",
    @SerializedName("items") val items: List<EpisodeItemDto> = emptyList()
)

data class EpisodeItemDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("slug") val slug: String = "",
    @SerializedName("embed") val embed: String = ""
)
