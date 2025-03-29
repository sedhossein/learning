package com.sed.url_shortener.datasource.mock

import com.sed.url_shortener.datasource.URLDataSource
import com.sed.url_shortener.model.URL
import org.springframework.stereotype.Repository

@Repository
class MockURLDataSource : URLDataSource {

    private val urls = listOf(URL(1, "http://original.com", "short"))

    override fun get(shortenURL: String): URL {
        TODO("Not yet implemented")
    }

    override fun save(org: String, shorten: String): Int {
        TODO("Not yet implemented")
    }

    override fun update(oldShorten: String, newShorten: String) {
        TODO("Not yet implemented")
    }

    override fun delete(shorten: String) {
        TODO("Not yet implemented")
    }

    override fun all(): Collection<URL>? {
        return urls
    }

}