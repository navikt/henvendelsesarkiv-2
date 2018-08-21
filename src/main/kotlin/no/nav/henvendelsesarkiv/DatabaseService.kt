package no.nav.henvendelsesarkiv

import no.nav.henvendelsesarkiv.model.Arkivpost
import no.nav.henvendelsesarkiv.model.ArkivpostMapper
import no.nav.henvendelsesarkiv.model.Vedlegg
import no.nav.henvendelsesarkiv.model.VedleggMapper
import org.springframework.jdbc.core.JdbcTemplate

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

class DatabaseService constructor(private val jt: JdbcTemplate, private val useHsql: Boolean = false) {

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
        arkivpost.arkivpostId = nextSequenceValue()
        insertIntoDb(arkivpost)
        arkivpost.vedleggListe.forEach{ opprettVedlegg(arkivpost.arkivpostId, it) }
        return arkivpost.arkivpostId
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
            setBlob(it, 8, vedlegg.dokument)
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
}