package com.sed.url_shortener.datasource

import com.sed.url_shortener.model.URL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp

@Repository
@Qualifier("dbDataSource")
class DBRepository(private val db: JdbcTemplate) : URLDataSource {

    override fun get(shortenURL: String): URL {
        return db.queryForObject(
            "SELECT * FROM urls WHERE shorten_url = ? LIMIT 1",
            { response, _ ->
                URL(
                    response.getInt("id"),
                    response.getString("original_url"),
                    response.getString("shorten_url"),
                    response.getTimestamp("created_at"),
                    response.getTimestamp("updated_at")
                )
            },
            shortenURL
        ) ?: throw NoSuchElementException("URL with shortenURL '$shortenURL' not found")
    }

    override fun save(org: String, shorten: String): Int {
        val keyHolder = GeneratedKeyHolder()
        val now = Timestamp(System.currentTimeMillis())

        db.update({ connection ->
            val ps = connection.prepareStatement(
                "INSERT INTO urls (original_url, shorten_url, created_at, updated_at) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setString(1, org)
            ps.setString(2, shorten)
            ps.setTimestamp(3, now)
            ps.setTimestamp(4, now)
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Int
            ?: throw IllegalStateException("Failed to retrieve generated ID")

        return generatedId
    }

    override fun update(oldShorten: String, newShorten: String) {
        try {
            db.update { connection ->
                val ps = connection.prepareStatement(
                    "UPDATE urls SET shorten_url = ?, updated_at = ? WHERE shorten_url = ?",
                    Statement.RETURN_GENERATED_KEYS
                )
                ps.setString(1, newShorten)
                ps.setTimestamp(2, Timestamp(System.currentTimeMillis()))
                ps.setString(3, oldShorten)
                ps
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override fun delete(shorten: String) {
        try {
            db.update("DELETE FROM urls WHERE shorten_url = ?", shorten)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun all(): List<URL>? {
        val sql = "SELECT * FROM urls"
        return db.query(sql) { rs: ResultSet, _: Int ->
            URL(
                id = rs.getInt("id"),
                original = rs.getString("original_url"),
                shorten = rs.getString("shorten_url"),
                createdAt = rs.getTimestamp("created_at"),
                updatedAt = rs.getTimestamp("updated_at")
            )
        }
    }

}
