package com.watchmetrics.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(TmdbProperties::class)
class TmdbConfig {

    @Bean
    fun tmdbRestClient(properties: TmdbProperties, builder: RestClient.Builder): RestClient {
        val clientBuilder = builder
            .baseUrl(properties.baseUrl)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)

        if (properties.accessToken.isNotBlank()) {
            clientBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${properties.accessToken}")
        }

        return clientBuilder.build()
    }
}
