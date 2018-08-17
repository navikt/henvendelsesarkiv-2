package no.nav.henvendelsesarkiv.model

import no.nav.henvendelsesarkiv.boolverdi
import no.nav.henvendelsesarkiv.tallverdi
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.util.*

data class Vedlegg(
        val arkivpostId: Long,
        val filnavn: String?,
        val filtype: String?,
        val variantformat: String?,
        val tittel: String?,
        val brevkode: String?,
        val strukturert: Boolean?,
        val dokument: ByteArray?
) {
    fun insertParams(): LinkedHashMap<String, Any> {
        val map = LinkedHashMap<String, Any>()
        map["arkivpostId"] = arkivpostId
        filnavn?.let { map["filnavn"] = it }
        filtype?.let { map["filtype"] = it }
        variantformat?.let { map["variantformat"] = it }
        tittel?.let { map["tittel"] = it }
        brevkode?.let { map["brevkode"] = it }
        strukturert?.let { map["strukturert"] = tallverdi(it) }
        dokument?.let { map["dokument"] = it }
        return map
    }
}

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
                rs.getBytes("dokument")
        )
    }
}