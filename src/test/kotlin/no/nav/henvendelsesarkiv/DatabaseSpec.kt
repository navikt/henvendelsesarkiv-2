package no.nav.henvendelsesarkiv

import no.nav.henvendelsesarkiv.model.Arkivpost
import no.nav.henvendelsesarkiv.model.Vedlegg
import org.amshove.kluent.*
import org.jetbrains.spek.api.*
import org.jetbrains.spek.api.dsl.*
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime

private const val TOM_ARKIVPOST_ID: Long = 1
private const val ARKIVPOST_ID_VEDLEGG: Long = 2

object DatabaseSpec : Spek({
    lateinit var jt: JdbcTemplate
    lateinit var db: DatabaseService
    describe("DatabaseService") {
        beforeGroup {
            jt = testJdbcTemplate()
            db = DatabaseService(jt, true)
        }

        beforeEachTest {
            createSequence(jt)
            createArkivpost(jt)
            createVedlegg(jt)
        }

        given("DatabaseService exists") {
            on("insert arkivpost without vedlegg") {
                it("db should contain 1 arkivpost") {
                    db.opprettHenvendelse(lagTomArkivpost())
                    val count = jt.queryForObject("SELECT COUNT(*) FROM arkivpost", Int::class.java)
                    count `should equal` 1
                }

                it("db should contain 0 vedlegg") {
                    db.opprettHenvendelse(lagTomArkivpost())
                    val count = jt.queryForObject("SELECT COUNT(*) FROM vedlegg", Int::class.java)
                    count `should equal` 0
                }

                it("hentArkivpost fetches correct arkivpost") {
                    db.opprettHenvendelse(lagTomArkivpost())
                    val arkivpost = db.hentHenvendelse(TOM_ARKIVPOST_ID)
                    arkivpost?.temagruppe `should equal` "TEMAGRUPPE1"
                }
            }

            on("insert arkivpost with vedlegg") {
                it("db should contain 2 vedlegg") {
                    db.opprettHenvendelse(lagArkivpostMedVedlegg())
                    val count = jt.queryForObject("SELECT COUNT(*) FROM vedlegg", Int::class.java)
                    count `should equal` 2
                }

                it("hentArkivpost fetches arkivpost with 2 vedlegg") {
                    db.opprettHenvendelse(lagArkivpostMedVedlegg())
                    val arkivpost = db.hentHenvendelse(ARKIVPOST_ID_VEDLEGG)
                    arkivpost?.vedleggListe?.size `should equal` 2
                }
            }
        }
    }
})

private fun lagTomArkivpost(): Arkivpost {
    return Arkivpost(
            TOM_ARKIVPOST_ID,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
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
            "STATUS1",
            "KATEGORIKODE1",
            true,
            true,
            false,
            true
    )
}

private fun lagArkivpostMedVedlegg(): Arkivpost {
    return Arkivpost(
            ARKIVPOST_ID_VEDLEGG,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
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
            "STATUS1",
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
            ARKIVPOST_ID_VEDLEGG,
            "FILNAVN",
            "FILTYPE",
            "VARIANT",
            "TITTEL",
            "BREVKODE",
            true,
            "DOKUMENT".toByteArray()
    )
}