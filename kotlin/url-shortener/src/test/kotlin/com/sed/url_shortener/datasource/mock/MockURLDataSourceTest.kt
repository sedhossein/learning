package com.sed.url_shortener.datasource.mock

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse


class MockURLDataSourceTest {

    private var mockURLDataSource = MockURLDataSource() // Instantiate directly

    @Test
    fun `should select and get the data in list`() {
        // give
        val shorten = "11"

        // when
        val url = mockURLDataSource.get(shorten)

        // then
        assert(url.id == 1)
        assert(url.original == "http://one.com")
    }

    @Test
    fun all() {
        val urls = mockURLDataSource.all()

        assertFalse(urls.isNullOrEmpty())
        assertEquals(3, urls.size)
    }

    // and so on ...
}