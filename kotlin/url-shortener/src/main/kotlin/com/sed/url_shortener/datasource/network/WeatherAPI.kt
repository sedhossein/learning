package com.sed.url_shortener.datasource.network

import com.sed.url_shortener.model.Forecast
import java.io.Serializable

interface WeatherAPI {
    fun get(name: String): WeatherResponse
}

data class WeatherResponse(
    val temperature: String,
    val wind: String,
    val description: String,
    val forecast: List<Forecast>,
) : Serializable
