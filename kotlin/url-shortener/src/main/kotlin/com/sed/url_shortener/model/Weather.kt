package com.sed.url_shortener.model

import java.io.Serializable

data class Forecast(
    val day: String,
    val temperature: String,
    val wind: String,
) : Serializable
