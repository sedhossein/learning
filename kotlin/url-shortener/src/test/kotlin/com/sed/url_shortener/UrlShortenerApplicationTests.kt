package com.sed.url_shortener

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class UrlShortenerApplicationTests {
    @Test
    fun contextLoads() {
        // Test passes if Spring context loads
    }
}
