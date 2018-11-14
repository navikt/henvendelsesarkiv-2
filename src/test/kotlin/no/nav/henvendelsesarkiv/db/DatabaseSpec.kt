package no.nav.henvendelsesarkiv.db

import no.nav.henvendelsesarkiv.model.ArkivStatusType
import no.nav.henvendelsesarkiv.model.Arkivpost
import no.nav.henvendelsesarkiv.model.Vedlegg
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.util.*

object DatabaseSpec : Spek({
    lateinit var jt: JdbcTemplate
    lateinit var db: DatabaseService
    describe("DatabaseService") {
        beforeGroup {
            jt = testJdbcTemplate()
            db = DatabaseService(jt, true)
        }

        beforeEachTest {
            jt.execute("SET DATABASE SQL SYNTAX ORA TRUE")
            createSequence(jt)
            createArkivpostTable(jt)
            createVedleggTable(jt)
        }

        given("DatabaseService exists") {
            on("insert arkivpost without vedlegg") {
                db.opprettHenvendelse(lagTomArkivpost())

                it("db should contain 1 arkivpost") {
                    val count = jt.queryForObject("SELECT COUNT(*) FROM arkivpost", Int::class.java)
                    count `should equal` 1
                }

                it("db should not have saved null as arkivpostId") {
                    val arkivpostId = jt.queryForObject("SELECT arkivpostid FROM arkivpost", Int::class.java)
                    arkivpostId `should not be` null
                }

                it("db should contain 0 vedlegg") {
                    val count = jt.queryForObject("SELECT COUNT(*) FROM vedlegg", Int::class.java)
                    count `should equal` 0
                }

                it("hentArkivpost fetches correct arkivpost") {
                    val arkivpost = db.hentHenvendelse(1)
                    arkivpost?.temagruppe `should equal` "TEMAGRUPPE1"
                }

                it("hentTemagrupper fetches one temagruppe") {
                    val temagruppe = db.hentTemagrupper("AKTØRID1")
                    temagruppe.size `should equal` 1
                    temagruppe[0].arkivpostId `should equal` "1"
                }
            }

            on("insert arkivpost with vedlegg") {
                db.opprettHenvendelse(lagArkivpostMedVedlegg())

                it("db should contain 2 vedlegg") {
                    val count = jt.queryForObject("SELECT COUNT(*) FROM vedlegg", Int::class.java)
                    count `should equal` 2
                }

                it("hentArkivpost fetches arkivpost with 2 vedlegg") {
                    val arkivpost = db.hentHenvendelse(1)
                    arkivpost?.vedleggListe?.size `should equal` 2
                }

                it("should have valid dokument") {
                    val arkivpost = db.hentHenvendelse(1)
                    arkivpost?.vedleggListe?.get(0)?.dokument `should not be` null
                }
            }

            on("update utgår dato") {
                val nyTid = LocalDateTime.now().plusHours(5).withNano(0)
                db.opprettHenvendelse(lagTomArkivpost())
                db.settUtgaarDato(1, nyTid)

                it("should have a new date") {
                    val arkivpost = db.hentHenvendelse(1)
                    arkivpost?.utgaarDato `should equal` nyTid
                }
            }

            on("insert multiple arkivposter") {
                db.opprettHenvendelse(lagArkivpostMedVedlegg(0))
                db.opprettHenvendelse(lagArkivpostMedVedlegg(1))
                db.opprettHenvendelse(lagArkivpostMedVedlegg(2))
                db.opprettHenvendelse(lagArkivpostMedVedlegg(3))
                db.opprettHenvendelse(lagArkivpostMedVedlegg(4))

                it("should contain 5 arkivposter") {
                    val liste = db.hentHenvendelserForAktoer("AKTØRID1", null, null, null)
                    liste.size `should equal` 5
                }

                it("should be correct with max") {
                    val liste = db.hentHenvendelserForAktoer("AKTØRID1", null, null, 3)
                    liste.size `should equal` 3
                }

                it("should be in right interval") {
                    val liste = db.hentHenvendelserForAktoer("AKTØRID1", LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), null)
                    liste.size `should equal` 2
                }

                it("should contain vedlegg") {
                    val liste = db.hentHenvendelserForAktoer("AKTØRID1", null, null, null)
                    val arkivpost = liste[0]
                    arkivpost.vedleggListe.size `should equal` 2
                    val vedlegg = arkivpost.vedleggListe[0]
                    vedlegg.filnavn `should equal` "FILNAVN"
                }
            }

            on("kassering") {
                db.opprettHenvendelse(lagTomArkivpostIFortiden())
                db.opprettHenvendelse(lagTomArkivpostIFortiden())
                db.opprettHenvendelse(lagArkivpostMedVedlegg(1))
                db.opprettHenvendelse(lagArkivpostMedVedlegg(1))
                db.kasserUtgaatteHenvendelser()

                it("should have kassert 4 vedlegg") {
                    val count = jt.queryForObject("SELECT COUNT(*) FROM vedlegg WHERE dokument IS NULL", Int::class.java)
                    count `should equal` 4
                }

                it("arkivpost has status KASSERT") {
                    val arkivpost = db.hentHenvendelse(1)
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
            sensitiv = true)

    arkivpost.vedleggListe.add(lagVedlegg(1))
    arkivpost.vedleggListe.add(lagVedlegg(1))

    return arkivpost
}