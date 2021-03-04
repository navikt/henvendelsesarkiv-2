package no.nav.henvendelsesarkiv.abac

import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

private const val OIDC_TOKEN = "ewogICJzdWIiOiAidGVzdHVzZXIiLAogICJ2ZXIiOiAiMS4wIiwKICAiaXNzIjogImh0dHBzOi8vdG9rZW4tc2VydmljZSIsCiAgImF1ZCI6IFsKICAgICJ0ZXN0IgogIF0sCiAgImFjciI6ICIwIiwKICAibmJmIjogMTU0MzgzNzU5NCwKICAiYXpwIjogInRlc3R1c2VyIiwKICAiaWRlbnRUeXBlIjogIlVzZXIiLAogICJhdXRoX3RpbWUiOiAxNTQzODM3NTk0LAogICJ1dHkiOiAiU3lzdGVtcmVzc3VycyIsCiAgImV4cCI6IDE1NDM4NDEyMjQsCiAgImlhdCI6IDE1NDM4Mzc1OTQKfQ=="

object AbacCacheSpec : Spek({
    describe("An ABAC cache") {
        lateinit var cache: AbacCache

        given("Cache initialized") {
            on("Cache with one entry") {
                cache = AbacCache()
                cache.storeResultOfLookup(OIDC_TOKEN, "METHOD", "ACTION", true)
                it("Should answer true on check") {
                    val hasAccess = cache.hasAccess(OIDC_TOKEN, "METHOD", "ACTION")
                    hasAccess `should equal` true
                }
                it("Should overwrite token") {
                    cache.storeResultOfLookup(OIDC_TOKEN, "METHOD", "ACTION", false)
                    val hasAccess = cache.hasAccess(OIDC_TOKEN, "METHOD", "ACTION")
                    hasAccess `should equal` false
                }
            }
        }

        given("Cache initialized with -1 expiry to ensure expiry") {
            on("Cache with one entry") {
                cache = AbacCache(-1)
                cache.storeResultOfLookup(OIDC_TOKEN, "METHOD", "ACTION", true)
                it("Should return null") {
                    val hasAccess = cache.hasAccess(OIDC_TOKEN, "METHOD", "ACTION")
                    hasAccess `should be` null
                }
            }
        }
    }
})
