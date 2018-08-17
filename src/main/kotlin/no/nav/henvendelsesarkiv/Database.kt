package no.nav.henvendelsesarkiv

import no.nav.henvendelsesarkiv.model.Arkivpost
import no.nav.henvendelsesarkiv.model.ArkivpostMapper
import no.nav.henvendelsesarkiv.model.Vedlegg
import no.nav.henvendelsesarkiv.model.VedleggMapper
import org.springframework.jdbc.core.JdbcTemplate

class Database constructor(private val jt: JdbcTemplate, private val useHsql: Boolean = false) {

    fun hentHenvendelse(id: String): Arkivpost? {
        val arkivpostSql = "SELECT * FROM arkivpost WHERE arkivpostId = ?"
        val vedleggSql = "SELECT * FROM vedlegg WHERE arkivpostId = ?"
        val arkivpost = jt.queryForObject(arkivpostSql, ArkivpostMapper(), id)
        if(arkivpost != null) {
            jt.query(vedleggSql, VedleggMapper(), id).forEach { arkivpost.vedleggListe.add(it) }
        }
        return arkivpost
    }

    fun opprettHenvendelse(arkivpost: Arkivpost): Long {
        arkivpost.arkivpostId = nextSequenceValue()
        val sql = """
            INSERT INTO arkivpost(
                ${arkivpost.insertParams().keys.joinToString()}
            ) VALUES (
                ${arrayOfNulls<String>(arkivpost.insertParams().size).fillIt("?").joinToString()}
            )
            """
        jt.update(sql, arkivpost.insertParams().values)
        arkivpost.vedleggListe.forEach{ opprettVedlegg(it) }
        return arkivpost.arkivpostId
    }

    private fun opprettVedlegg(vedlegg: Vedlegg) {
        val sql = """
            INSERT INTO arkivpost(
                ${vedlegg.insertParams().keys.joinToString()}
            ) VALUES (
                ${arrayOfNulls<String>(vedlegg.insertParams().size).fillIt("?").joinToString()}
            )
            """
        jt.update(sql, vedlegg.insertParams().values)
    }

    private fun nextSequenceValue(): Long {
        val sql = if(useHsql) {
            "CALL NEXT VALUE FOR arkivpostId_seq"
        } else {
            "SELECT arkivpostId_seq.nextval FROM dual"
        }
        return jt.queryForObject(sql, Long::class.java)
    }

    private fun Array<String?>.fillIt(str: String): Array<String?> {
        this.fill(str)
        return this
    }

}