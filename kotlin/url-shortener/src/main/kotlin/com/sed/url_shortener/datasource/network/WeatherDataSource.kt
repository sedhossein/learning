package com.sed.url_shortener.datasource.network

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Service
class WeatherDataSource(
    @Autowired private val rest: RestTemplate,
) : WeatherAPI {

    private val baseURL = "https://goweather.xyz/weather"

    override fun get(name: String): WeatherResponse {
        return rest.getForObject("$baseURL/$name")
    }
}