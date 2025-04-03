package com.sed.url_shortener.controller.v1

import com.sed.url_shortener.controller.Base
import com.sed.url_shortener.controller.Response
import com.sed.url_shortener.service.StorageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*


@RestController
@RequestMapping("/api/v1")
class URLControllerV1(private val storage: StorageService) : Base() {
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
        } catch (e: Throwable) {
            logger.error("Fetch url failed: {}", e.toString())
        }

        return ResponseEntity
            .status(307) // HttpStatus.TEMPORARY_REDIRECT
            .location(URI.create(address))
            .build()
    }

    @PostMapping("url")
    @ResponseStatus(HttpStatus.CREATED)
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

data class StoreRequest(val original: String, val shorten: String?)

data class UpdateRequest(val shorten: String)
