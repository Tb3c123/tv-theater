package com.tvtheater.app.data.api

import com.google.gson.Gson
import com.tvtheater.app.data.api.dto.FilmDetailResponse
import com.tvtheater.app.data.api.dto.FilmListResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NguonCApiDtoTest {

    private val gson = Gson()

    @Test
    fun `test parsing FilmListResponse JSON`() {
        val sampleJson = """
        {
          "status": "success",
          "paginate": {
            "current_page": 1,
            "total_page": 10,
            "total_items": 100,
            "items_per_page": 10
          },
          "items": [
            {
              "name": "One Piece",
              "slug": "one-piece",
              "original_name": "One Piece",
              "thumb_url": "https://img.nguonc.com/images/thumb1.jpg",
              "poster_url": "https://img.nguonc.com/images/poster1.jpg",
              "total_episodes": 2000,
              "current_episode": "Tập 1180",
              "time": "24 phút/tập",
              "quality": "HD",
              "language": "Vietsub",
              "year": "1999",
              "description": "Hành trình tìm kiếm kho báu One Piece"
            }
          ]
        }
        """.trimIndent()

        val response = gson.fromJson(sampleJson, FilmListResponse::class.java)

        assertEquals("success", response.status)
        assertEquals(1, response.paginate.currentPage)
        assertEquals(10, response.paginate.totalPage)
        assertEquals(1, response.items.size)

        val movie = response.items[0]
        assertEquals("One Piece", movie.name)
        assertEquals("one-piece", movie.slug)
        assertEquals("HD", movie.quality)
        assertEquals(2000, movie.totalEpisodes)
    }

    @Test
    fun `test parsing FilmDetailResponse JSON with episodes and embed stream`() {
        val sampleJson = """
        {
          "status": "success",
          "movie": {
            "name": "Digimon Beatbreak",
            "slug": "digimon-beatbreak",
            "original_name": "Digimon Beatbreak",
            "thumb_url": "https://phim.nguonc.com/thumb.jpg",
            "poster_url": "https://phim.nguonc.com/poster.jpg",
            "total_episodes": 50,
            "current_episode": "Tập 48",
            "time": "23 phút/tập",
            "quality": "HD",
            "language": "Vietsub",
            "director": "Konaka",
            "casts": "Chiba, Takahashi",
            "year": "2025",
            "description": "Câu chuyện quái vật số trong kỷ nguyên e-Pulse",
            "episodes": [
              {
                "server_name": "Vietsub #1",
                "items": [
                  {
                    "name": "1",
                    "slug": "tap-1",
                    "embed": "https://embed12.streamc.xyz/embed.php?hash=0a738920f88ca0a2d8da6edb9c1d7bbc"
                  },
                  {
                    "name": "2",
                    "slug": "tap-2",
                    "embed": "https://embed13.streamc.xyz/embed.php?hash=7e65bc503757033b19e945a53ca54cd3"
                  }
                ]
              }
            ]
          }
        }
        """.trimIndent()

        val response = gson.fromJson(sampleJson, FilmDetailResponse::class.java)

        assertEquals("success", response.status)
        assertNotNull(response.movie)
        assertEquals("Digimon Beatbreak", response.movie.name)
        assertEquals(1, response.movie.episodes.size)

        val server = response.movie.episodes[0]
        assertEquals("Vietsub #1", server.serverName)
        assertEquals(2, server.items.size)

        val ep1 = server.items[0]
        assertEquals("1", ep1.name)
        assertEquals("tap-1", ep1.slug)
        assertTrue(ep1.embed.contains("embed.php?hash="))
    }
}
