package com.sed.url_shortener.service

import com.sed.url_shortener.datasource.URLDataSource
import com.sed.url_shortener.model.URL
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
internal class StorageServiceTest {

    private val cacheDataSource: URLDataSource = mockk()

    private val dbDataSource: URLDataSource = mockk()

    private val storageService = StorageService(cacheDataSource, dbDataSource)

    @BeforeEach
    fun setUp() {
        clearAllMocks() // Reset mock state before each test
    }

    @Nested
    @DisplayName("get from datasource")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetFromDatasource {
        @Test
        fun `should get url from cache datasource`() = runTest {
            // given
            val expectedUrl = URL(1, "google.com", "gg")

            coEvery { cacheDataSource.get(expectedUrl.shorten) } returns expectedUrl

            // when
            val url = storageService.get(expectedUrl.shorten)

            // then
            assertEquals(expectedUrl, url)
            coVerify(exactly = 1) { cacheDataSource.get(expectedUrl.shorten) }
            coVerify(exactly = 0) { dbDataSource.get(expectedUrl.shorten) }
        }

        @Test
        fun `should get error from cache and get from db but updating cache failed`() = runTest {
            // give
            val expectedUrl = URL(1, "google.com", "gg")

            coEvery { cacheDataSource.get(expectedUrl.shorten) } throws Exception("redis is down")
            coEvery { dbDataSource.get(expectedUrl.shorten) } returns expectedUrl
            coEvery {
                cacheDataSource.save(
                    expectedUrl.original,
                    expectedUrl.shorten
                )
            } throws Exception("redis still is down")

            // when
            val url = storageService.get(expectedUrl.shorten)

            // then
            assertEquals(expectedUrl, url)
            coVerify(exactly = 1) { cacheDataSource.get(expectedUrl.shorten) }
            coVerify(exactly = 1) { cacheDataSource.save(expectedUrl.original, expectedUrl.shorten) }
            coVerify(exactly = 1) { dbDataSource.get(expectedUrl.shorten) }
        }

        @Test
        fun `should get error from both db and cache`() = runTest {
            // give
            val expectedException = Exception("network issue happens")
            val givenUrl = URL(1, "google.com", "gg")

            coEvery { cacheDataSource.get(givenUrl.shorten) } throws Exception("redis is down")
            coEvery { dbDataSource.get(givenUrl.shorten) } throws expectedException

            // when/then
            val exception = assertThrows<Exception> {
                storageService.get(givenUrl.shorten)
            }

            // then
            assertEquals(expectedException.message, exception.message)
            coVerify(exactly = 1) { cacheDataSource.get(givenUrl.shorten) }
            coVerify(exactly = 0) { cacheDataSource.save(givenUrl.original, givenUrl.shorten) }
            coVerify(exactly = 1) { dbDataSource.get(givenUrl.shorten) }
        }
    }

    // and so on ...
}