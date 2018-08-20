package no.nav.henvendelsesarkiv

import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

fun lagDateTime(ts: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), TimeZone.getDefault().toZoneId())

fun hentMillisekunder(ldt: LocalDateTime): Long =
        ldt.atZone(TimeZone.getDefault().toZoneId()).toInstant().toEpochMilli()

fun boolverdi(n: Int): Boolean = n == 1

fun setString(ps: PreparedStatement, index: Int, value: String?) {
    if(value != null) {
        ps.setString(index, value)
    } else {
        ps.setNull(index, Types.VARCHAR)
    }
}

fun setLong(ps: PreparedStatement, index: Int, value: Long?) {
    if(value == null || value < 1) {
        ps.setNull(index, Types.NUMERIC)
    } else {
        ps.setLong(index, value)
    }
}

fun setBoolean(ps: PreparedStatement, index: Int, value: Boolean) {
    ps.setInt(index, if(value) 1 else 0)
}

fun setTimestamp(ps: PreparedStatement, index: Int, value: LocalDateTime?) {
    if(value != null) {
        ps.setTimestamp(index, Timestamp(hentMillisekunder(value)))
    } else {
        ps.setNull(index, Types.TIMESTAMP)
    }
}

fun setBlob(ps: PreparedStatement, index: Int, value: ByteArray?) {
    if(value != null) {
        ps.setBlob(index, value.inputStream())
    } else {
        ps.setNull(index, Types.BLOB)
    }
}