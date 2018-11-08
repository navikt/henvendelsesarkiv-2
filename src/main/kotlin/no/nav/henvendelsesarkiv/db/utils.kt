package no.nav.henvendelsesarkiv.db

import com.google.gson.*
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import java.time.format.DateTimeFormatter


private val formatter = DateTimeFormatter.ISO_DATE_TIME

fun lagDateTime(ts: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), TimeZone.getDefault().toZoneId())

fun lagDateTime(str: String): LocalDateTime =
        LocalDateTime.parse(str, formatter)

fun hentMillisekunder(ldt: LocalDateTime): Long =
        ldt.atZone(TimeZone.getDefault().toZoneId()).toInstant().toEpochMilli()

fun boolverdi(n: Int): Boolean = n == 1

fun setString(ps: PreparedStatement, index: Int, value: String?) {
    if (value != null) {
        ps.setString(index, value)
    } else {
        ps.setNull(index, Types.VARCHAR)
    }
}

fun setLong(ps: PreparedStatement, index: Int, value: Long?) {
    if (value == null || value < 1) {
        ps.setNull(index, Types.NUMERIC)
    } else {
        ps.setLong(index, value)
    }
}

fun setBoolean(ps: PreparedStatement, index: Int, value: Boolean) {
    ps.setInt(index, if (value) 1 else 0)
}

fun setTimestamp(ps: PreparedStatement, index: Int, value: LocalDateTime?) {
    if (value != null) {
        ps.setTimestamp(index, Timestamp(hentMillisekunder(value)))
    } else {
        ps.setNull(index, Types.TIMESTAMP)
    }
}

fun setBlob(ps: PreparedStatement, index: Int, value: ByteArray?) {
    if (value != null) {
        ps.setBlob(index, value.inputStream())
    } else {
        ps.setNull(index, Types.BLOB)
    }
}

val localDateTimeSerializer: JsonSerializer<LocalDateTime> = JsonSerializer { src, _, _ ->
    if (src == null) null else JsonPrimitive(src.format(formatter))
}

val localDateTimeDeserializer: JsonDeserializer<LocalDateTime> = JsonDeserializer<LocalDateTime> { json, _, _ ->
    if (json == null) null else LocalDateTime.parse(json.asString, formatter)
}