package no.nav.henvendelsesarkiv.model

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

data class ArkivpostTemagruppe(
        val arkivpostId: String,
        val aktoerId: String?,
        val fodselsnummer: String?,
        val temagruppe: String?,
        val arkivStatusType: ArkivStatusType?
)

private val logger = LoggerFactory.getLogger(ArkivpostTemagruppeMapper::class.java)

class ArkivpostTemagruppeMapper : RowMapper<ArkivpostTemagruppe> {

    override fun mapRow(rs: ResultSet, rowNum: Int): ArkivpostTemagruppe {

        logger.debug("### Skal hente temagruppe ###")
        logger.debug("arkpostID: " + rs.getLong("arkivpostId").toString() )
        logger.debug("fodselsnummer: " +  rs.getString("fodselsnummer") )
        logger.debug("aktoerId: " +   rs.getString("fodselsnummer") )
        logger.debug("temagruppe: " +   rs.getString("temagruppe") )
        logger.debug("status: " +   rs.getString("status") )
        logger.debug("status mapped: " +  rs.getString("status")?.let { ArkivStatusType.valueOf(it)})

        return ArkivpostTemagruppe(
                rs.getLong("arkivpostId").toString(),
                rs.getString("aktoerId"),
                rs.getString("fodselsnummer"),
                rs.getString("temagruppe"),
                rs.getString("status")?.let { ArkivStatusType.valueOf(it) }
        )
    }
}