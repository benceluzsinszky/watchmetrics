package com.watchmetrics.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(OmdbProperties::class)
class OmdbConfig {

    @Bean
    fun omdbRestClient(properties: OmdbProperties, builder: RestClient.Builder): RestClient =
        builder
            .baseUrl(properties.baseUrl)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build()
}
