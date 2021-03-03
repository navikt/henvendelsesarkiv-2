package no.nav.henvendelsesarkiv.model

import no.nav.henvendelsesarkiv.db.boolverdi
import no.nav.henvendelsesarkiv.db.lagDateTime
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.LocalDateTime

data class Arkivpost(
    var arkivpostId: Long? = null,
    val arkivertDato: LocalDateTime?,
    val mottattDato: LocalDateTime?,
    val utgaarDato: LocalDateTime?,
    val temagruppe: String?,
    val arkivpostType: String?,
    val dokumentType: String?,
    val kryssreferanseId: String?,
    val kanal: String?,
    val aktoerId: String?,
    val fodselsnummer: String?,
    val navIdent: String?,
    val innhold: String?,
    val journalfoerendeEnhet: String?,
    val status: String?,
    val kategorikode: String?,
    val signert: Boolean,
    val erOrganInternt: Boolean,
    val begrensetPartInnsyn: Boolean,
    val sensitiv: Boolean,
    val vedleggListe: ArrayList<Vedlegg> = ArrayList()
)

class ArkivpostMapper : RowMapper<Arkivpost> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Arkivpost {
        return Arkivpost(
            rs.getLong("arkivpostId"),
            rs.getTimestamp("arkivertDato")?.let { lagDateTime(it.time) },
            rs.getTimestamp("mottattDato")?.let { lagDateTime(it.time) },
            rs.getTimestamp("utgaarDato")?.let { lagDateTime(it.time) },
            rs.getString("temagruppe"),
            rs.getString("arkivpostType"),
            rs.getString("dokumentType"),
            rs.getString("kryssreferanseId"),
            rs.getString("kanal"),
            rs.getString("aktoerId"),
            rs.getString("fodselsnummer"),
            rs.getString("navIdent"),
            rs.getString("innhold"),
            rs.getString("journalfoerendeEnhet"),
            rs.getString("status"),
            rs.getString("kategorikode"),
            boolverdi(rs.getInt("signert")),
            boolverdi(rs.getInt("erOrganInternt")),
            boolverdi(rs.getInt("begrensetPartInnsyn")),
            boolverdi(rs.getInt("sensitiv"))
        )
    }
}
