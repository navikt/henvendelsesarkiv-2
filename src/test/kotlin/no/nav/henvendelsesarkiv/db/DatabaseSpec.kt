package no.nav.henvendelsesarkiv.db

import no.nav.henvendelsesarkiv.model.ArkivStatusType
import no.nav.henvendelsesarkiv.model.Arkivpost
import no.nav.henvendelsesarkiv.model.Vedlegg
import org.amshove.kluent.*
import org.jetbrains.spek.api.*
import org.jetbrains.spek.api.dsl.*
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime

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
            }

            on("update utgår dato") {
                val nyTid = LocalDateTime.now().plusHours(5)
                db.opprettHenvendelse(lagTomArkivpost())
                db.settUtgaarDato(1, nyTid)

                it("should have a new date") {
                    val arkivpost = db.hentHenvendelse(1)
                    arkivpost?.utgaarDato `should equal` nyTid
                }
            }

            on("insert multiple arkivposter") {
                db.opprettHenvendelse(lagTomArkivpost(0))
                db.opprettHenvendelse(lagTomArkivpost(1))
                db.opprettHenvendelse(lagTomArkivpost(2))
                db.opprettHenvendelse(lagTomArkivpost(3))
                db.opprettHenvendelse(lagTomArkivpost(4))

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
            0,
            LocalDateTime.now().plusHours(plusstimer),
            LocalDateTime.now().plusHours(plusstimer),
            LocalDateTime.now().plusHours(plusstimer),
            "TEMAGRUPPE1",
            "ARKIVPOSTTYPE1",
            "DOKUMENTTYPE1",
            "KRYSSREFID1",
            "KANAL1",
            "AKTØRID1",
            "FØDSELSNUMMER1",
            "NAVIDENT1",
            "INNHOLD1",
            "JOURNALFØRENDEENHET1",
            "ARKIVERT",
            "KATEGORIKODE1",
            true,
            true,
            false,
            true
    )
}

private fun lagArkivpostMedVedlegg(plusstimer: Long = 0): Arkivpost {
    return Arkivpost(
            0,
            LocalDateTime.now().plusHours(plusstimer),
            LocalDateTime.now().plusHours(plusstimer),
            LocalDateTime.now().plusHours(plusstimer),
            "TEMAGRUPPE1",
            "ARKIVPOSTTYPE1",
            "DOKUMENTTYPE1",
            "KRYSSREFID1",
            "KANAL1",
            "AKTØRID1",
            "FØDSELSNUMMER1",
            "NAVIDENT1",
            "INNHOLD1",
            "JOURNALFØRENDEENHET1",
            "ARKIVERT",
            "KATEGORIKODE1",
            true,
            true,
            false,
            true,
            ArrayList(listOf(lagVedlegg(), lagVedlegg()))
    )
}

private fun lagVedlegg(): Vedlegg {
    return Vedlegg(
            0,
            "FILNAVN",
            "FILTYPE",
            "VARIANT",
            "TITTEL",
            "BREVKODE",
            true,
            "DOKUMENT".toByteArray()
    )
}

private fun lagTomArkivpostIFortiden(): Arkivpost {
    return Arkivpost(
            0,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().minusHours(1),
            "TEMAGRUPPE1",
            "ARKIVPOSTTYPE1",
            "DOKUMENTTYPE1",
            "KRYSSREFID1",
            "KANAL1",
            "AKTØRID1",
            "FØDSELSNUMMER1",
            "NAVIDENT1",
            "INNHOLD1",
            "JOURNALFØRENDEENHET1",
            "ARKIVERT",
            "KATEGORIKODE1",
            true,
            true,
            false,
            true,
            ArrayList(listOf(lagVedlegg(), lagVedlegg()))
    )
}