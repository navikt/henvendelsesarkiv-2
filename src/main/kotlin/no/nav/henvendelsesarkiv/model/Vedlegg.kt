package no.nav.henvendelsesarkiv.model

import no.nav.henvendelsesarkiv.db.boolverdi
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.util.*

data class Vedlegg(
        val arkivpostId: Long?,
        val filnavn: String?,
        val filtype: String?,
        val variantformat: String?,
        val tittel: String?,
        val brevkode: String?,
        val strukturert: Boolean,
        val dokument: String?
)

class VedleggMapper : RowMapper<Vedlegg> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Vedlegg {
        return Vedlegg(
                rs.getLong("arkivpostId"),
                rs.getString("filnavn"),
                rs.getString("filtype"),
                rs.getString("variantformat"),
                rs.getString("tittel"),
                rs.getString("brevkode"),
                boolverdi(rs.getInt("strukturert")),
                rs.getBytes("dokument")?.let { Base64.getEncoder().encodeToString(it) }
        )
    }
}