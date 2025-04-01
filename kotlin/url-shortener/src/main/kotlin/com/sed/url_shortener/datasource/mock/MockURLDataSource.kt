package com.sed.url_shortener.datasource.mock

import com.sed.url_shortener.NotFoundExceptions
import com.sed.url_shortener.datasource.URLDataSource
import com.sed.url_shortener.model.URL
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
class MockURLDataSource : URLDataSource {

    private val urls = mutableListOf(
        URL(1, "http://one.com", "11"),
        URL(2, "http://two.com", "22"),
        URL(3, "http://three.com", "33"),
    )

    override fun get(shortenURL: String): URL {
        return urls.find { it.shorten == shortenURL } ?: throw NotFoundExceptions()
    }

    override fun save(org: String, shorten: String): Int {
        val newID = urls.size
        urls.plus(
            URL(
                id = urls.size,
                original = org,
                shorten = shorten,
            )
        )

        return newID
    }

    override fun update(oldShorten: String, newShorten: String) {
        val idx = urls.indexOfFirst { it.shorten == oldShorten }.also {
            if (it == -1) throw NotFoundExceptions()
        }

        val oldURL = urls[idx]
        urls.removeAt(idx)
        urls.add(
            idx, URL(
                oldURL.id,
                oldURL.original,
                newShorten,
                oldURL.createdAt,
                Timestamp(
                    System.currentTimeMillis(),
                )
            )
        )
    }

    override fun delete(shorten: String) {
        val idx = urls.indexOfFirst { it.shorten == shorten }.also {
            if (it == -1) throw NotFoundExceptions()
        }

        urls.removeAt(idx)
    }

    override fun all(): Collection<URL>? {
        return urls
    }

}