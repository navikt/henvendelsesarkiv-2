package no.nav.henvendelsesarkiv.db

import no.nav.henvendelsesarkiv.model.*
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.PreparedStatementSetter
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

private val logger = LoggerFactory.getLogger(SelectService::class.java)

class SelectService constructor(val jt: CoroutineAwareJdbcTemplate = coroutineAwareJdbcTemplate) {
    suspend fun sjekkDatabase(): String =
            try {
                jt.use {
                    queryForObject("SELECT COUNT(1) FROM arkivpost", Integer::class.java)
                }
                "OK"
            } catch (e: Exception) {
                logger.error("Klarte ikke å koble opp mot database", e)
                e.message ?: "Feil, ingen feilmelding gitt"
            }


    suspend fun hentHenvendelse(id: Long): Arkivpost? {
        val arkivpostSql = "SELECT * FROM arkivpost WHERE arkivpostId = ?"
        val vedleggSql = "SELECT * FROM vedlegg WHERE arkivpostId = ?"
        val arkivpost = jt.use {
            queryForObject(arkivpostSql, ArkivpostMapper(), id)
        }
        if (arkivpost != null) {
            jt.use { query(vedleggSql, VedleggMapper(), id) }.forEach { arkivpost.vedleggListe.add(it) }
        }
        return arkivpost
    }

    suspend fun hentTemagrupper(aktoerId: String): List<ArkivpostTemagruppe> {
        val temagruppeSql = "SELECT arkivpostId, aktoerId, fodselsnummer, temagruppe, status FROM arkivpost WHERE aktoerId = ?"
        return jt.use {
            query(temagruppeSql, PreparedStatementSetter { it.setString(1, aktoerId) }, ArkivpostTemagruppeMapper())
        }
    }

    suspend fun hentHenvendelserForAktoer(aktoerId: String, fra: LocalDateTime?, til: LocalDateTime?, max: Int?): List<Arkivpost> {
        val fraTekst = fra?.let { " AND mottattDato >= ? " } ?: ""
        val tilTekst = til?.let { " AND mottattDato <= ? " } ?: ""
        val sql = wrapInMax("""
            SELECT * FROM arkivpost WHERE aktoerId = ?
            $fraTekst $tilTekst
            ORDER BY mottattDato DESC
        """.trimIndent(), max)

        val arkivposter = jt.use {
            query(sql, setParams(aktoerId, fra, til), ArkivpostMapper())
        }
        leggTilVedlegg(arkivposter)
        return arkivposter
    }

    private fun wrapInMax(sql: String, max: Int?): String = max?.let { "SELECT * FROM ($sql) WHERE ROWNUM <= $it" }
            ?: sql

    private fun setParams(aktoerId: String, fra: LocalDateTime?, til: LocalDateTime?): PreparedStatementSetter {
        var i = 1
        return PreparedStatementSetter { ps ->
            ps.setString(i++, aktoerId)
            fra?.let { ps.setTimestamp(i++, Timestamp(hentMillisekunder(it))) }
            til?.let { ps.setTimestamp(i++, Timestamp(hentMillisekunder(it))) }
        }
    }

    private suspend fun leggTilVedlegg(arkivposter: List<Arkivpost>) {
        val alleId = arkivposter.map { it.arkivpostId }.toList()
        val alleVedlegg = ArrayList<Vedlegg>()
        alleId.asSequence().chunked(1000).forEach {
            val vedlegg = jt.use {
                query("select * from vedlegg where arkivpostId in (${it.joinToString(",")})", VedleggMapper())
            }
            alleVedlegg.addAll(vedlegg)
        }
        alleVedlegg.forEach {
            val ap = arkivposter.find { a -> a.arkivpostId == it.arkivpostId }
            ap?.vedleggListe?.add(it)
        }
    }
}
