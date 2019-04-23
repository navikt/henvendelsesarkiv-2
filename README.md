# Henvendelsesarkiv
Applikasjon som tar imot henvendelser fra bruker via modia og lagrer disse.

# Førstegangsoppsett på Utviklerimage
* I home-mappen din i katalogen .gradle (eksempelvis C:\Users\v140448\\.gradle) legges en gradle.properties fil med følgende innhold:
```
systemProp.javax.net.ssl.trustStore=C:/Users/<DIN_IDENT>/.gradle/nav_truststore_nonproduction_ny2.jts
systemProp.javax.net.ssl.trustStorePassword=<PASSORD_FRA_FASIT>
systemProp.http.proxyHost=webproxy-utvikler.nav.no
systemProp.http.proxyPort=8088
systemProp.http.nonProxyHosts=localhost|127.0.0.1|*.local|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no|*devel
systemProp.https.proxyHost=webproxy-utvikler.nav.no
systemProp.https.proxyPort=8088
systemProp.https.nonProxyHosts=localhost|127.0.0.1|*.local|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no|*devel
```
* Legg inn din ident og passord fra fasit i gradle.properties.
* Hent sertifikatet nav_truststore_nonproduction_ny2.jts fra fasit: https://fasit.adeo.no/resources/3816117?revision=3901254 og legg det i samme mappe 
* Hent sertifikat for gradle her: https://plugins.gradle.org/m2/ (klikk på hengelåsen og last ned .cer-fil) og legg det i samme mappe.
* Benytt F:\programvare\kse-51 eller skriv kommando på kommandolinje for å legge gradle-sertifikatet inn i nav-truststore-sertifikatet
* Åpne folderen til prosjektet i IntelliJ og benytt IntelliJ sin gradle-wrapper.
* Hvis nedlastinga henger på gradle, så kan det være proxy-settings i IntelliJ, den skal stå på auto detect
* Du skal ikke se bort ifra at du må oppdatere Kotlin plugin også
* Kryss fingrene, deretter: Tut og kjør!

# Teste lokalt
Fyll ut de manglende feltene i StartKtorLocal.kt, ved å legge inn verdier i `user.home/localstart.properties` og kjør.

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* Daniel Winsvold, daniel.winsvold@nav.no
* Jan-Eirik B. Nævdal, jan.eirik.b.navdal@nav.no
* Jørund Amsen, jorund.amsen@nav.no
* Richard Borge, richard.borge@nav.no

### For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-personoversikt .
