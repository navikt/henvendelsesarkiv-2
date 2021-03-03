package no.nav.henvendelsesarkiv.db

import kotlinx.coroutines.runBlocking
import no.nav.henvendelsesarkiv.model.ArkivStatusType
import no.nav.henvendelsesarkiv.model.Arkivpost
import no.nav.henvendelsesarkiv.model.Vedlegg
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import org.jetbrains.spek.api.dsl.it
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.util.*

fun SpecBody.con(description: String, body: suspend ActionBody.() -> Unit) {
    this.on(description) { runBlocking { body() } }
}
fun TestContainer.cit(description: String, body: suspend TestBody.() -> Unit) {
    this.it(description) { runBlocking { body() } }
}

object DatabaseSpec : Spek({
    lateinit var jt: JdbcTemplate
    lateinit var selectService: SelectService
    lateinit var updateService: UpdateService
    describe("SelectService") {
        beforeGroup {
            val coroutineAwareJdbcTemplate = CoroutineAwareJdbcTemplate(testDatsource())
            jt = coroutineAwareJdbcTemplate.jdbcTemplate
            selectService = SelectService(coroutineAwareJdbcTemplate)
            updateService = UpdateService(coroutineAwareJdbcTemplate, true)
        }

        beforeEachTest {
            jt.execute("SET DATABASE SQL SYNTAX ORA TRUE")
            createSequence(jt)
            createArkivpostTable(jt)
            createVedleggTable(jt)
        }

        given("SelectService exists") {
            on("sjekk database") {
                cit("should give ok") {
                    updateService.opprettHenvendelse(lagTomArkivpost())
                    val melding = selectService.sjekkDatabase()
                    melding `should be equal to` "OK"
                }
            }
            con("insert arkivpost without vedlegg") {
                updateService.opprettHenvendelse(lagTomArkivpost())

                cit("db should contain 1 arkivpost") {
                    val count = jt.queryForObject("SELECT COUNT(*) FROM arkivpost", Int::class.java)
                    count `should equal` 1
                }

                cit("db should not have saved null as arkivpostId") {
                    val arkivpostId = jt.queryForObject("SELECT arkivpostid FROM arkivpost", Int::class.java)
                    arkivpostId `should not be` null
                }

                cit("db should contain 0 vedlegg") {
                    val count = jt.queryForObject("SELECT COUNT(*) FROM vedlegg", Int::class.java)
                    count `should equal` 0
                }

                cit("hentArkivpost fetches correct arkivpost") {
                    val arkivpost = selectService.hentHenvendelse(1)
                    arkivpost?.temagruppe `should equal` "TEMAGRUPPE1"
                }

                cit("hentTemagrupper fetches one temagruppe") {
                    val temagruppe = selectService.hentTemagrupper("AKTØRID1")
                    temagruppe.size `should equal` 1
                    temagruppe[0].arkivpostId `should equal` "1"
                }
            }

            con("insert arkivpost with vedlegg") {
                updateService.opprettHenvendelse(lagArkivpostMedVedlegg())

                cit("db should contain 2 vedlegg") {
                    val count = jt.queryForObject("SELECT COUNT(*) FROM vedlegg", Int::class.java)
                    count `should equal` 2
                }

                cit("hentArkivpost fetches arkivpost with 2 vedlegg") {
                    val arkivpost = selectService.hentHenvendelse(1)
                    arkivpost?.vedleggListe?.size `should equal` 2
                }

                cit("should have valid dokument") {
                    val arkivpost = selectService.hentHenvendelse(1)
                    arkivpost?.vedleggListe?.get(0)?.dokument `should not be` null
                }
            }

            con("update utgår dato") {
                val nyTid = LocalDateTime.now().plusHours(5).withNano(0)
                updateService.opprettHenvendelse(lagTomArkivpost())
                updateService.settUtgaarDato(1, nyTid)

                cit("should have a new date") {
                    val arkivpost = selectService.hentHenvendelse(1)
                    arkivpost?.utgaarDato `should equal` nyTid
                }
            }

            con("insert multiple arkivposter") {
                updateService.opprettHenvendelse(lagArkivpostMedVedlegg(0))
                updateService.opprettHenvendelse(lagArkivpostMedVedlegg(1))
                updateService.opprettHenvendelse(lagArkivpostMedVedlegg(2))
                updateService.opprettHenvendelse(lagArkivpostMedVedlegg(3))
                updateService.opprettHenvendelse(lagArkivpostMedVedlegg(4))

                cit("should contain 5 arkivposter") {
                    val liste = selectService.hentHenvendelserForAktoer("AKTØRID1", null, null, null)
                    liste.size `should equal` 5
                }

                cit("should be correct with max") {
                    val liste = selectService.hentHenvendelserForAktoer("AKTØRID1", null, null, 3)
                    liste.size `should equal` 3
                }

                cit("should be in right interval") {
                    val liste = selectService.hentHenvendelserForAktoer("AKTØRID1", LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), null)
                    liste.size `should equal` 2
                }

                cit("should contain vedlegg") {
                    val liste = selectService.hentHenvendelserForAktoer("AKTØRID1", null, null, null)
                    val arkivpost = liste[0]
                    arkivpost.vedleggListe.size `should equal` 2
                    val vedlegg = arkivpost.vedleggListe[0]
                    vedlegg.filnavn `should equal` "FILNAVN"
                }
            }

            con("kassering") {
                updateService.opprettHenvendelse(lagTomArkivpostIFortiden())
                updateService.opprettHenvendelse(lagTomArkivpostIFortiden())
                updateService.opprettHenvendelse(lagArkivpostMedVedlegg(1))
                updateService.opprettHenvendelse(lagArkivpostMedVedlegg(1))
                updateService.kasserUtgaatteHenvendelser()

                cit("should have kassert 4 vedlegg") {
                    val count = jt.queryForObject("SELECT COUNT(*) FROM vedlegg WHERE dokument IS NULL", Int::class.java)
                    count `should equal` 4
                }

                cit("arkivpost has status KASSERT") {
                    val arkivpost = selectService.hentHenvendelse(1)
                    arkivpost?.status `should equal` ArkivStatusType.KASSERT.name
                }
            }
        }
    }
})

private fun lagTomArkivpost(plusstimer: Long = 0): Arkivpost {
    return Arkivpost(
        arkivertDato = LocalDateTime.now().plusHours(plusstimer),
        mottattDato = LocalDateTime.now().plusHours(plusstimer),
        utgaarDato = LocalDateTime.now().plusHours(plusstimer),
        temagruppe = "TEMAGRUPPE1",
        arkivpostType = "ARKIVPOSTTYPE1",
        dokumentType = "DOKUMENTTYPE1",
        kryssreferanseId = "KRYSSREFID1",
        kanal = "KANAL1",
        aktoerId = "AKTØRID1",
        fodselsnummer = "FØDSELSNUMMER1",
        navIdent = "NAVIDENT1",
        innhold = "INNHOLD1",
        journalfoerendeEnhet = "JOURNALFØRENDEENHET1",
        status = "ARKIVERT",
        kategorikode = "KATEGORIKODE1",
        signert = true,
        erOrganInternt = true,
        begrensetPartInnsyn = false,
        sensitiv = true
    )
}

private fun lagArkivpostMedVedlegg(plusstimer: Long = 0): Arkivpost {
    val arkivpost = Arkivpost(
        arkivertDato = LocalDateTime.now().plusHours(plusstimer),
        mottattDato = LocalDateTime.now().plusHours(plusstimer),
        utgaarDato = LocalDateTime.now().plusHours(plusstimer),
        temagruppe = "TEMAGRUPPE1",
        arkivpostType = "ARKIVPOSTTYPE1",
        dokumentType = "DOKUMENTTYPE1",
        kryssreferanseId = "KRYSSREFID1",
        kanal = "KANAL1",
        aktoerId = "AKTØRID1",
        fodselsnummer = "FØDSELSNUMMER1",
        navIdent = "NAVIDENT1",
        innhold = "INNHOLD1",
        journalfoerendeEnhet = "JOURNALFØRENDEENHET1",
        status = "ARKIVERT",
        kategorikode = "KATEGORIKODE1",
        signert = true,
        erOrganInternt = true,
        begrensetPartInnsyn = false,
        sensitiv = true
    )

    arkivpost.vedleggListe.add(lagVedlegg(1))
    arkivpost.vedleggListe.add(lagVedlegg(1))

    return arkivpost
}

private fun lagVedlegg(arkivpostId: Long): Vedlegg {
    return Vedlegg(
        arkivpostId,
        "FILNAVN",
        "FILTYPE",
        "VARIANT",
        "TITTEL",
        "BREVKODE",
        true,
        Base64.getEncoder().encodeToString("DOKUMENT".toByteArray())
    )
}

private fun lagTomArkivpostIFortiden(): Arkivpost {
    val arkivpost = Arkivpost(
        arkivertDato = LocalDateTime.now().minusHours(1),
        mottattDato = LocalDateTime.now().minusHours(1),
        utgaarDato = LocalDateTime.now().minusHours(1),
        temagruppe = "TEMAGRUPPE1",
        arkivpostType = "ARKIVPOSTTYPE1",
        dokumentType = "DOKUMENTTYPE1",
        kryssreferanseId = "KRYSSREFID1",
        kanal = "KANAL1",
        aktoerId = "AKTØRID1",
        fodselsnummer = "FØDSELSNUMMER1",
        navIdent = "NAVIDENT1",
        innhold = "INNHOLD1",
        journalfoerendeEnhet = "JOURNALFØRENDEENHET1",
        status = "ARKIVERT",
        kategorikode = "KATEGORIKODE1",
        signert = true,
        erOrganInternt = true,
        begrensetPartInnsyn = false,
        sensitiv = true
    )

    arkivpost.vedleggListe.add(lagVedlegg(1))
    arkivpost.vedleggListe.add(lagVedlegg(1))

    return arkivpost
}
