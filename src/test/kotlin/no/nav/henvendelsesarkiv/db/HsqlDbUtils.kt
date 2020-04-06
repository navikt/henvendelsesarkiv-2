package no.nav.henvendelsesarkiv.db

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import javax.sql.DataSource

fun testDatsource(): DataSource {
    val ds = SingleConnectionDataSource()
    ds.setSuppressClose(true)
    ds.setDriverClassName("org.hsqldb.jdbcDriver")
    ds.url = "jdbc:hsqldb:mem:henvendelsesarkiv"
    ds.username = "user"
    ds.password = ""
    return ds
}

fun createSequence(jt: JdbcTemplate) {
    jt.execute("DROP SEQUENCE arkivpostId_seq IF EXISTS")
    jt.execute("CREATE SEQUENCE arkivpostId_seq AS INTEGER START WITH 1 INCREMENT BY 1")
}

fun createArkivpostTable(jt: JdbcTemplate) {
    jt.execute("DROP TABLE arkivpost IF EXISTS")
    jt.execute("""
        CREATE TABLE arkivpost (
            arkivpostId NUMERIC NOT NULL,
            arkivertDato TIMESTAMP,
            mottattDato TIMESTAMP,
            utgaarDato TIMESTAMP,
            temagruppe VARCHAR(255),
            arkivpostType VARCHAR(255),
            dokumentType VARCHAR(255),
            kryssreferanseId VARCHAR(255),
            kanal VARCHAR(255),
            aktoerId VARCHAR(255),
            fodselsnummer VARCHAR(255),
            navIdent VARCHAR(255),
            innhold VARCHAR(255),
            journalfoerendeEnhet VARCHAR(255),
            status VARCHAR(255),
            kategorikode VARCHAR(255),
            signert NUMERIC,
            erOrganInternt NUMERIC,
            begrensetPartInnsyn NUMERIC,
            sensitiv NUMERIC
        )
    """.trimIndent())
}

fun createVedleggTable(jt: JdbcTemplate) {
    jt.execute("DROP TABLE vedlegg IF EXISTS")
    jt.execute("""
        CREATE TABLE vedlegg (
            arkivpostId NUMERIC NOT NULL,
            filnavn VARCHAR(255),
            filtype VARCHAR(255),
            variantformat VARCHAR(255),
            tittel VARCHAR(255),
            brevkode VARCHAR(255),
            strukturert NUMERIC,
            dokument BLOB
        )
    """.trimIndent())
}
