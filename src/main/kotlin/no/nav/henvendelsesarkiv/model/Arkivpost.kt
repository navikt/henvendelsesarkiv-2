package no.nav.henvendelsesarkiv.model

import no.nav.henvendelsesarkiv.boolverdi
import no.nav.henvendelsesarkiv.hentMillisekunder
import no.nav.henvendelsesarkiv.lagDateTime
import no.nav.henvendelsesarkiv.tallverdi
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.LocalDateTime

data class Arkivpost(
    var arkivpostId: Long,
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
    val sensitivt: Boolean,
    val vedleggListe: ArrayList<Vedlegg> = ArrayList()
) {
    fun insertParams(): LinkedHashMap<String, Any> {
        val map = LinkedHashMap<String, Any>()
        map["arkivpostId"] = arkivpostId
        arkivertDato?.let { map["arkivertDato"] = hentMillisekunder(it) }
        mottattDato?.let { map["mottattDato"] = hentMillisekunder(it) }
        utgaarDato?.let { map["utgaarDato"] = hentMillisekunder(it) }
        temagruppe?.let { map["temagruppe"] = it }
        arkivpostType?.let { map["arkivpostType"] = it }
        dokumentType?.let { map["dokumentType"] = it }
        kryssreferanseId?.let { map["kryssreferanseId"] = it }
        kanal?.let { map["kanal"] = it }
        aktoerId?.let { map["aktoerId"] = it }
        fodselsnummer?.let { map["fodselsnummer"] = it }
        navIdent?.let { map["navIdent"] = it }
        innhold?.let { map["innhold"] = it }
        journalfoerendeEnhet?.let { map["journalfoerendeEnhet"] = it }
        status?.let { map["status"] = it }
        kategorikode?.let { map["kategorikode"] = it }
        signert?.let { map["signert"] = tallverdi(it) }
        erOrganInternt?.let { map["erOrganInternt"] = tallverdi(it) }
        sensitivt?.let { map["sensitivt"] = tallverdi(it) }
        return map;
    }
}

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
                boolverdi(rs.getInt("sensitivt"))
        )
    }
}



