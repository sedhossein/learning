package com.sed.url_shortener.controller

import com.sed.url_shortener.service.StorageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
class URLController(val storage: StorageService) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(URLController::class.java)
    }

    @GetMapping("/")
    fun index(@RequestParam(name = "name", defaultValue = "guest", required = false) name: String?): String {
        return "Hello, $name! Here is url shortener with Kotlin and localstack(aws:Redis+Postgresql)"
    }
}

@RestController
@RequestMapping("/api/v1")
class URLControllerV1(storage: StorageService) : URLController(storage) {
    @GetMapping("/urls")
    fun all(): ResponseEntity<Response> {
        val urls = storage.all()
        return ResponseEntity.ok(
            Response(
                200, mapOf(
                    "message" to "done",
                    "urls" to urls,
                )
            )
        )
    }

    @GetMapping("/url/{id}")
    suspend fun redirect(@PathVariable id: String): ResponseEntity<Void> {
        var address = "/"

        try {
            val url = storage.get(id)
            if (url != null) {
                address = url.original
            }

            logger.info("Successfully fetched url: {}", url)
        } catch (e: Exception) {
            logger.error("Fetch url failed: {}", e.toString())
        }

        return ResponseEntity
            .status(HttpStatus.TEMPORARY_REDIRECT)
            .location(URI.create(address)).build()
    }

    @PostMapping("url")
    fun store(@RequestBody req: StoreRequest): ResponseEntity<Response> {
        val shorten = req.shorten ?: UUID.randomUUID().toString()
        val id = storage.save(req.original, shorten)

        return ResponseEntity.ok().body(
            Response(
                200, mapOf(
                    "message" to "saved",
                    "id" to id,
                )
            )
        )
    }

    @PatchMapping("/url/{id}")
    fun update(@PathVariable id: String, @RequestBody req: UpdateRequest): ResponseEntity<Response> {
        storage.update(id, req.shorten)

        return ResponseEntity.ok().body(Response(200, mapOf("message" to "updated")))
    }

    @DeleteMapping("/url/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Response> {
        storage.delete(id)

        return ResponseEntity.ok().body(Response(200, mapOf("message" to "deleted")))
    }

}

data class Response(val status: Int, val result: Map<String, Any?>)

data class StoreRequest(val original: String, val shorten: String?)

data class UpdateRequest(val shorten: String)
