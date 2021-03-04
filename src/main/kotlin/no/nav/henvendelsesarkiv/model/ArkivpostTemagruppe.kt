package no.nav.henvendelsesarkiv.model

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

data class ArkivpostTemagruppe(
    val arkivpostId: String,
    val aktoerId: String?,
    val fodselsnummer: String?,
    val temagruppe: String?,
    val arkivStatusType: ArkivStatusType?
)

class ArkivpostTemagruppeMapper : RowMapper<ArkivpostTemagruppe> {
    override fun mapRow(rs: ResultSet, rowNum: Int): ArkivpostTemagruppe {
        return ArkivpostTemagruppe(
            rs.getLong("arkivpostId").toString(),
            rs.getString("aktoerId"),
            rs.getString("fodselsnummer"),
            rs.getString("temagruppe"),
            rs.getString("status")?.let { ArkivStatusType.valueOf(it) }
        )
    }
}
