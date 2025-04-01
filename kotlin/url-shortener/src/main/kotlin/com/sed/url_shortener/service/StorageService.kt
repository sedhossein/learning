package com.sed.url_shortener.service

import com.sed.url_shortener.NotFoundExceptions
import com.sed.url_shortener.datasource.URLDataSource
import com.sed.url_shortener.model.URL
import jakarta.transaction.Transactional
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class StorageService @Autowired constructor(
    @Qualifier("cacheDataSource") private val cache: URLDataSource,
    @Qualifier("dbDataSource") private val db: URLDataSource,
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(StorageService::class.java)
        private val requestScope = CoroutineScope(Dispatchers.IO) // Scope for request related coroutines
        private const val CACHE_UPDATE_TIMEOUT_MS = 5000L // 5 seconds
    }

    fun all(): Collection<URL>? {
        return db.all()
    }

    suspend fun get(shorten: String): URL? = withContext(Dispatchers.IO) {
        try {
            cache.get(shorten).also {
                logger.debug("cache.get($shorten) hit")
                return@withContext it
            }
        } catch (e: NotFoundExceptions) {
            logger.debug("cache.get($shorten) missed")
        } catch (e: Throwable) {
            logger.warn("cache.get($shorten) failed: ${e.message}")
        }

        try {
            val url = db.get(shorten)
            requestScope.launch {
                withTimeoutOrNull(CACHE_UPDATE_TIMEOUT_MS) {
                    try {
                        cache.save(url.original, shorten)
                        logger.debug("Cache updated for $shorten.")
                    } catch (e: Exception) {
                        logger.warn("Cache update for $shorten failed: ${e.message}")
                    }
                } ?: run {
                    logger.warn("Cache update for $shorten timed out.")
                }
            }

            url // Return db result
        } catch (e: NotFoundExceptions) {
            logger.debug("db.get($shorten) missed")
            throw e
        } catch (e: Throwable) {
            logger.error("db.get($shorten) failed: ${e.message}")
            throw e
        }
    }


    fun save(org: String, shorten: String): Int? {
        return try {
            val id = db.save(org, shorten)
            requestScope.launch {
                withTimeoutOrNull(CACHE_UPDATE_TIMEOUT_MS) {
                    try {
                        cache.save(org, shorten)
                        logger.debug("Cache updated for $shorten.")
                    } catch (e: Exception) {
                        logger.warn("Cache update for $shorten failed: ${e.message}", e)
                    }
                } ?: run {
                    logger.warn("Cache update for $shorten timed out.")
                }
            }

            id
        } catch (e: Exception) {
            logger.error("save failed: ${e.message}", e)
            0
        }
    }

    @Transactional
    fun update(oldShorten: String, newShorten: String) {
        try {
            cache.update(oldShorten, newShorten)
            db.update(oldShorten, newShorten)
        } catch (e: Exception) {
            logger.error("update failed: ${e.message}", e)
            throw e // Re-throw the exception to trigger rollback
        }

        logger.info("url shorten changed from ($oldShorten) to ($newShorten)")
    }

    @Transactional
    fun delete(shorten: String) {
        try {
            cache.delete(shorten)
            db.delete(shorten)
        } catch (e: Exception) {
            logger.error("delete failed: ${e.message}", e)
            throw e // Re-throw the exception to trigger rollback
        }

        logger.info("url shorten($shorten) deleted")
    }

}





