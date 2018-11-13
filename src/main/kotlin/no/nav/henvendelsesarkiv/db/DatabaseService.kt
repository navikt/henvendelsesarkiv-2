package no.nav.henvendelsesarkiv.db

import no.nav.henvendelsesarkiv.model.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementSetter
import java.sql.Timestamp
import java.time.LocalDateTime
import no.nav.henvendelsesarkiv.model.Arkivpost
import org.slf4j.LoggerFactory
import java.util.*


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

private const val TIMEOUT_FOR_JOBB_TIMER: Long = 4

class DatabaseService constructor(private val jt: JdbcTemplate = hikariJdbcTemplate, private val useHsql: Boolean = false) {

    val log = LoggerFactory.getLogger("henvendelsesarkiv.ArkivpostRoutes")

    fun hentHenvendelse(id: Long): Arkivpost? {
        val arkivpostSql = "SELECT * FROM arkivpost WHERE arkivpostId = ?"
        val vedleggSql = "SELECT * FROM vedlegg WHERE arkivpostId = ?"
        val arkivpost = jt.queryForObject(arkivpostSql, ArkivpostMapper(), id)
        if (arkivpost != null) {
            jt.query(vedleggSql, VedleggMapper(), id).forEach { arkivpost.vedleggListe.add(it) }
        }
        return arkivpost
    }

    fun opprettHenvendelse(arkivpost: Arkivpost): Long {
        log.info("#### DB start ####")
        arkivpost.arkivpostId = nextSequenceValue()
        log.info("#### next value: " + arkivpost.arkivpostId +  "####")
        insertIntoDb(arkivpost)
        log.info("#### DB end ####")
        //TODO: for test.
        //arkivpost.vedleggListe.forEach{ opprettVedlegg(arkivpost.arkivpostId, it) }
        return arkivpost.arkivpostId!!
    }

    fun hentTemagrupper(aktoerId: String): List<ArkivpostTemagruppe> {
        val temagruppeSql = "SELECT arkivpostId, aktoerId, fodselsnummer, temagruppe, status FROM arkivpost WHERE aktoerId = ?"
        return jt.query(temagruppeSql, PreparedStatementSetter { it.setString(1, aktoerId) }, ArkivpostTemagruppeMapper())
    }

    fun hentHenvendelserForAktoer(aktoerId: String, fra: LocalDateTime?, til: LocalDateTime?, max: Int?): List<Arkivpost> {
        val fraTekst = fra?.let { " AND mottattDato >= ? " } ?: ""
        val tilTekst = til?.let { " AND mottattDato <= ? " } ?: ""
        val sql = wrapInMax("""
            SELECT * FROM arkivpost WHERE aktoerId = ?
            $fraTekst $tilTekst
            ORDER BY mottattDato DESC
        """.trimIndent(), max)

        val arkivposter = jt.query(sql, setParams(aktoerId, fra, til), ArkivpostMapper())
        leggTilVedlegg(arkivposter)
        return arkivposter
    }

    fun settUtgaarDato(arkivpostId: Long, dato: LocalDateTime) {
        val sql = "UPDATE arkivpost SET utgaarDato = ? WHERE arkivpostId = ?"
        jt.update(sql, Timestamp(hentMillisekunder(dato)), arkivpostId)
    }

    fun kasserUtgaatteHenvendelser() {
        val terminereJobb = LocalDateTime.now().plusHours(TIMEOUT_FOR_JOBB_TIMER)
        val sql = "SELECT arkivpostId FROM arkivpost WHERE utgaarDato <= ?"
        val list = jt.queryForList(sql, Long::class.java, Timestamp(System.currentTimeMillis()))

        for (arkivpostId in list) {
            jt.update("UPDATE vedlegg SET dokument = NULL WHERE arkivpostId = ?", arkivpostId)
            jt.update("UPDATE arkivpost SET status = ? WHERE arkivpostId = ?", ArkivStatusType.KASSERT.name, arkivpostId)
            if (LocalDateTime.now().isAfter(terminereJobb)) {
                break
            }
        }
    }

    private fun wrapInMax(sql: String, max: Int?): String {
        return max?.let { "SELECT * FROM ($sql) WHERE ROWNUM <= $it" } ?: sql
    }

    private fun insertIntoDb(arkivpost: Arkivpost) {
        jt.update(ARKIVPOST_SQL) {
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

    private fun opprettVedlegg(arkivpostId: Long, vedlegg: Vedlegg) {

        jt.update(VEDLEGG_SQL) {
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

    private fun nextSequenceValue(): Long {
        val sql = if (useHsql) {
            "CALL NEXT VALUE FOR arkivpostId_seq"
        } else {
            "SELECT arkivpostId_seq.nextval FROM dual"
        }
        return jt.queryForObject(sql, Long::class.java) ?: 0
    }

    private fun setParams(aktoerId: String, fra: LocalDateTime?, til: LocalDateTime?): PreparedStatementSetter {
        var i = 1
        return PreparedStatementSetter { ps ->
            ps.setString(i++, aktoerId)
            fra?.let { ps.setTimestamp(i++, Timestamp(hentMillisekunder(it))) }
            til?.let { ps.setTimestamp(i++, Timestamp(hentMillisekunder(it))) }
        }
    }

    private fun leggTilVedlegg(arkivposter: List<Arkivpost>) {
        val alleId = arkivposter.map { it.arkivpostId }.toList()
        val alleVedlegg = ArrayList<Vedlegg>()
        alleId.asSequence().chunked(1000).forEach {
            alleVedlegg.addAll(jt.query("select * from vedlegg where arkivpostId in (${it.joinToString(",")})", VedleggMapper()))
        }
        alleVedlegg.forEach {
            val ap = arkivposter.find { a -> a.arkivpostId == it.arkivpostId }
            ap?.vedleggListe?.add(it)
        }

    }
}