package com.sed.url_shortener.controller.v1

import com.sed.url_shortener.datasource.network.WeatherAPI
import com.sed.url_shortener.datasource.network.WeatherResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
class WeatherController(private val weather: WeatherAPI) : V1() {

    @GetMapping("/weather/{name}")
    fun get(@PathVariable name: String): WeatherResponse {
        return weather.get(name)
    }
}

