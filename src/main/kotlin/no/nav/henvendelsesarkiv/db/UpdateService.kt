package no.nav.henvendelsesarkiv.db

import no.nav.henvendelsesarkiv.model.ArkivStatusType
import no.nav.henvendelsesarkiv.model.Arkivpost
import no.nav.henvendelsesarkiv.model.Vedlegg
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

private const val TIMEOUT_FOR_JOBB_TIMER: Long = 4

private const val ARKIVPOST_SQL = """
            INSERT INTO arkivpost(arkivpostId, arkivertDato, mottattDato, utgaarDato, temagruppe, arkivpostType, dokumentType,
                                  kryssreferanseId, kanal, aktoerId, fodselsnummer, navIdent, innhold, journalfoerendeEnhet,
                                  status, kategorikode, signert, erOrganInternt, begrensetPartInnsyn, sensitiv
            ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            )
            """

private const val VEDLEGG_SQL = """
            INSERT INTO vedlegg(
                arkivpostId, filnavn, filtype, variantformat, tittel, brevkode, strukturert, dokument
            ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?
            )
            """

private const val SKYGGE_OPPDATERING_SQL = """
            INSERT INTO skygge_vedlegg
                SELECT arkivpostid, SYSDATE, dokument FROM vedlegg
                WHERE arkivpostid = ?
            """

class UpdateService constructor(val jdbcTemplate: CoroutineAwareJdbcTemplate = coroutineAwareJdbcTemplate, private val useHsql: Boolean = false) {
    suspend fun opprettHenvendelse(arkivpost: Arkivpost): Long {
        return jdbcTemplate.inTransaction {
            arkivpost.arkivpostId = nextSequenceValue(this)
            opprettArkivpost(this, arkivpost)
            arkivpost.vedleggListe.forEach{ vedlegg -> opprettVedlegg(this, arkivpost.arkivpostId!!, vedlegg) }
            arkivpost.arkivpostId!!
        }
    }

    suspend fun kasserUtgaatteHenvendelser() {
        val terminereJobb = LocalDateTime.now().plusHours(TIMEOUT_FOR_JOBB_TIMER)
        val sql = "SELECT arkivpostId FROM arkivpost WHERE utgaarDato <= ? AND status != ${addDashes(ArkivStatusType.KASSERT.name)}"
        val list = jdbcTemplate.use { queryForList(sql, Long::class.java, Timestamp(System.currentTimeMillis())) }

        for (arkivpostId in list) {
            jdbcTemplate.use { update("UPDATE vedlegg SET dokument = NULL WHERE arkivpostId = ?", arkivpostId) }
            jdbcTemplate.use { update("UPDATE arkivpost SET status = ? WHERE arkivpostId = ?", ArkivStatusType.KASSERT.name, arkivpostId) }
            if (LocalDateTime.now().isAfter(terminereJobb)) {
                break
            }
        }
    }

    suspend fun settUtgaarDato(arkivpostId: Long, dato: LocalDateTime) {
        if (dato.isBefore(LocalDateTime.now())) {
            // Dette er et hint om at dette er en sletting, da skal vi lagre unna vedlegg i egen tabell
            // med 6 måneders angrefrist (gitt av joark-PO)
            jdbcTemplate.use { update(SKYGGE_OPPDATERING_SQL, arkivpostId) }
        }
        val sql = "UPDATE arkivpost SET utgaarDato = ? WHERE arkivpostId = ?"
        jdbcTemplate.use { update(sql, Timestamp(hentMillisekunder(dato)), arkivpostId) }
    }

    private fun opprettArkivpost(jdbcTemplate: JdbcTemplate, arkivpost: Arkivpost) {
        jdbcTemplate.update(ARKIVPOST_SQL) {
            setLong(it, 1, arkivpost.arkivpostId)
            setTimestamp(it, 2, arkivpost.arkivertDato)
            setTimestamp(it, 3, arkivpost.mottattDato)
            setTimestamp(it, 4, arkivpost.utgaarDato)
            setString(it, 5, arkivpost.temagruppe)
            setString(it, 6, arkivpost.arkivpostType)
            setString(it, 7, arkivpost.dokumentType)
            setString(it, 8, arkivpost.kryssreferanseId)
            setString(it, 9, arkivpost.kanal)
            setString(it, 10, arkivpost.aktoerId)
            setString(it, 11, arkivpost.fodselsnummer)
            setString(it, 12, arkivpost.navIdent)
            setString(it, 13, arkivpost.innhold)
            setString(it, 14, arkivpost.journalfoerendeEnhet)
            setString(it, 15, arkivpost.status)
            setString(it, 16, arkivpost.kategorikode)
            setBoolean(it, 17, arkivpost.signert)
            setBoolean(it, 18, arkivpost.erOrganInternt)
            setBoolean(it, 19, arkivpost.begrensetPartInnsyn)
            setBoolean(it, 20, arkivpost.sensitiv)
        }
    }

    private fun opprettVedlegg(jdbcTemplate: JdbcTemplate, arkivpostId: Long, vedlegg: Vedlegg) {

        jdbcTemplate.update(VEDLEGG_SQL) {
            setLong(it, 1, arkivpostId)
            setString(it, 2, vedlegg.filnavn)
            setString(it, 3, vedlegg.filtype)
            setString(it, 4, vedlegg.variantformat)
            setString(it, 5, vedlegg.tittel)
            setString(it, 6, vedlegg.brevkode)
            setBoolean(it, 7, vedlegg.strukturert)
            setBlob(it, 8, Base64.getDecoder().decode(vedlegg.dokument))
        }
    }

    private fun nextSequenceValue(jdbcTemplate: JdbcTemplate): Long {
        val sql = if (useHsql) {
            "CALL NEXT VALUE FOR arkivpostId_seq"
        } else {
            "SELECT arkivpostId_seq.nextval FROM dual"
        }
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0
    }

    private fun addDashes(str: String): String = "'$str'"
}
