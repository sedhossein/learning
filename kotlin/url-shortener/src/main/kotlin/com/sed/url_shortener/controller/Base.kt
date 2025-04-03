package com.sed.url_shortener.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController


@RestController
class Base {
    @ExceptionHandler(Throwable::class)
    fun handleMissedException(e: Throwable): ResponseEntity<Response> {
        return ResponseEntity.ok(
            Response(
                500, mapOf(
                    "message" to e.message,
                )
            )
        )
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Base::class.java)
    }
}

data class Response(val status: Int, val result: Map<String, Any?>)
