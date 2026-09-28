package com.tvtheater.app.domain.model

data class Movie(
    val slug: String,
    val name: String,
    val originalName: String? = null,
    val thumbUrl: String? = null,
    val posterUrl: String? = null,
    val quality: String = "HD",
    val currentEpisode: String? = null,
    val year: String? = null,
    val description: String? = null
)

data class MovieDetail(
    val slug: String,
    val name: String,
    val originalName: String? = null,
    val thumbUrl: String? = null,
    val posterUrl: String? = null,
    val quality: String = "HD",
    val currentEpisode: String? = null,
    val totalEpisodes: Int = 0,
    val time: String? = null,
    val director: String? = null,
    val casts: String? = null,
    val year: String? = null,
    val description: String? = null,
    val servers: List<ServerEpisode> = emptyList()
)

data class ServerEpisode(
    val serverName: String,
    val episodes: List<EpisodeItem>
)

data class EpisodeItem(
    val name: String,
    val slug: String,
    val embedUrl: String
)
