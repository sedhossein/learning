package com.sed.url_shortener.datasource

import com.sed.url_shortener.model.URL

//@Component
interface URLDataSource {
    fun get(shortenURL: String): URL
    fun save(org: String, shorten: String): Int
    fun update(oldShorten: String, newShorten: String)
    fun delete(shorten: String)
    fun all(): Collection<URL>?
}
