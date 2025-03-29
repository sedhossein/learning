package com.sed.url_shortener.model

import jakarta.persistence.*
import java.io.Serializable
import java.sql.Timestamp

@Entity
@Table(name = "urls")
data class URL(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "original_url", nullable = false)
    val original: String = "",

    @Column(name = "shorten_url", nullable = false, unique = true)
    val shorten: String = "",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Timestamp = Timestamp(System.currentTimeMillis()),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Timestamp = Timestamp(System.currentTimeMillis()),
) : Serializable