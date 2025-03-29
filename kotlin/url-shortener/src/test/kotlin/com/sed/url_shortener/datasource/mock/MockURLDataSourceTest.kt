package com.sed.url_shortener.datasource.mock

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse


class MockURLDataSourceTest {
    private val mockURLDataSource = MockURLDataSource()

    @Test
    fun get() {
    }

    @Test
    fun save() {
    }

    @Test
    fun update() {
    }

    @Test
    fun delete() {
    }

    @Test
    fun all() {
        val urls = mockURLDataSource.all()

        assertFalse(urls.isNullOrEmpty())
    }
}