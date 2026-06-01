package com.watchmetrics.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager =
        SimpleCacheManager().apply {
            setCaches(
                listOf(
                    cache(CacheNames.TMDB_SEARCH, Duration.ofMinutes(10), maxSize = 200),
                    cache(CacheNames.TMDB_SHOW, Duration.ofHours(6), maxSize = 500),
                    cache(CacheNames.TMDB_SEASON, Duration.ofHours(6), maxSize = 2_000),
                    cache(CacheNames.TMDB_EXTERNAL_IDS, Duration.ofHours(24), maxSize = 500),
                    cache(CacheNames.OMDB_TITLE, Duration.ofHours(6), maxSize = 500),
                    cache(CacheNames.OMDB_SEASON, Duration.ofHours(6), maxSize = 2_000),
                    cache(CacheNames.FINALE_VERDICT, Duration.ofHours(6), maxSize = 200),
                ),
            )
        }

    private fun cache(name: String, ttl: Duration, maxSize: Long): CaffeineCache =
        CaffeineCache(
            name,
            Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxSize)
                .build(),
        )
}

object CacheNames {
    const val TMDB_SEARCH = "tmdb-search"
    const val TMDB_SHOW = "tmdb-show"
    const val TMDB_SEASON = "tmdb-season"
    const val TMDB_EXTERNAL_IDS = "tmdb-external-ids"
    const val OMDB_TITLE = "omdb-title"
    const val OMDB_SEASON = "omdb-season"
    const val FINALE_VERDICT = "finale-verdict"
}
