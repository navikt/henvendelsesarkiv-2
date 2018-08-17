package no.nav.henvendelsesarkiv

import java.time.Instant
import java.time.LocalDateTime
import java.util.*

fun lagDateTime(ts: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), TimeZone.getDefault().toZoneId())

fun hentMillisekunder(ldt: LocalDateTime): Long =
        ldt.atZone(TimeZone.getDefault().toZoneId()).toInstant().toEpochMilli()

fun boolverdi(n: Int): Boolean = n == 1

fun tallverdi(b: Boolean): Int = if(b) 1 else 0
