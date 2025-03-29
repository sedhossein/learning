package com.sed.url_shortener.datasource

import com.sed.url_shortener.model.URL
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.util.concurrent.TimeUnit

@Repository
@Qualifier("cacheDataSource")
class CacheRepository(private val redisTemplate: RedisTemplate<String, URL>) : URLDataSource {

    private val ttl: Long = 1

    override fun get(shortenURL: String): URL {
        return redisTemplate.opsForValue().get(key(shortenURL))
            ?: throw Exception("redis get failed: null")
    }

    override fun save(org: String, shorten: String): Int {
        val url = URL(0, org, shorten)
        redisTemplate.opsForValue().set(key(shorten), url, ttl, TimeUnit.MINUTES)
        return 0
    }

    @Transactional
    override fun update(oldShorten: String, newShorten: String) {
        redisTemplate.opsForValue().get(oldShorten)?.also {
            val updatedUrl = URL(it.id, it.original, newShorten, it.createdAt, Timestamp(System.currentTimeMillis()))
            redisTemplate.opsForValue().set(key(newShorten), updatedUrl, ttl, TimeUnit.MINUTES)
            redisTemplate.delete(key(oldShorten))
        }
    }

    override fun delete(shorten: String) {
        redisTemplate.delete(key(shorten))
    }

    override fun all(): Collection<URL>? {
        TODO("not implemented. it's administrative private action, does not need caching")
    }

    private fun key(id: String): String = "urls:$id"
}
