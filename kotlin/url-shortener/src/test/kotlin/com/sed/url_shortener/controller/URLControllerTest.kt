package com.sed.url_shortener.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sed.url_shortener.datasource.mock.MockURLDataSource
import com.sed.url_shortener.model.URL
import com.sed.url_shortener.service.StorageService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(URLControllerTest.MockStorageServiceConfig::class)
internal class URLControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objMapper: ObjectMapper,
    val storageService: StorageService,
) {

    private val baseURL = "/api/v1"

    @Nested
    @DisplayName("GET /api/v1/urls")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetURLs {

        @Test
        fun `should return all requests from storage`() {
            // given
            val urls = MockURLDataSource().all() as List<URL>
            coEvery { storageService.all() } returns urls

            // when/then
            mockMvc.get("$baseURL/urls")
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.result.urls.[2].shorten") { value("33") } // Use "33"
                }

            coVerify(exactly = 1) { storageService.all() }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/url")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class CreateURL {
        @Test
        fun `should create new url and returns it's id`() {
            // give
            val newURL = StoreRequest("https://original.com", "short")
            coEvery { storageService.save(any(), any()) } returns 100

            // when
            val postPerformer = mockMvc.post("$baseURL/url") {
                contentType = MediaType.APPLICATION_JSON
                content = objMapper.writeValueAsString(newURL)
            }.andDo { print() }

            // then
            postPerformer.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.result.message") { value("saved") }
                jsonPath("$.result.id") { value(100) }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/url/{id}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class RedirectURL {
        @Test
        fun `should redirect to original url when found`() {
            // given
            val id = "22"
            val originalURL = "http://two.com"
            coEvery { storageService.get(id) } returns URL(2, originalURL, id)

            // when
            val mvcResult = mockMvc.get("$baseURL/url/$id")
                .andExpect { request { asyncStarted() } }
                .andReturn()

            // Wait for async processing and assert
            mockMvc.perform(asyncDispatch(mvcResult))
                .andDo { println() }
                .andExpect {
                    status().isTemporaryRedirect
                    header().string("Location", originalURL)
                }

            coVerify(exactly = 1) { storageService.get(id) }
        }


        @Test
        fun `should redirect to root when url not found`() {
            // given
            val id = "notFound"
            coEvery { storageService.get(id) } throws Exception("something went wrong")

            // when
            val mvcResult = mockMvc.get("$baseURL/url/$id")
                .andExpect { request { asyncStarted() } }
                .andReturn()

            // Wait for async completion
            mockMvc.perform(asyncDispatch(mvcResult))
                .andDo { println() }
                .andExpect {
                    status().isTemporaryRedirect
                    header().string("Location", "/")
                }

            coVerify(exactly = 1) { storageService.get(id) }
        }
    }

    // and so on ...

    // Configuration class to provide the mock bean
    class MockStorageServiceConfig {
        @Bean
        fun storageService(): StorageService = mockk()
    }
}