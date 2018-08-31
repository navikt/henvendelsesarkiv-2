package no.nav.henvendelsesarkiv.abac

import com.google.gson.GsonBuilder
import no.nav.henvendelsesarkiv.fasitProperties
import java.util.*


private val url = fasitProperties.abacEndpoint
private val gson = GsonBuilder().setPrettyPrinting().create()

private val ABAC_PDP_HEADERS = mapOf(
        "Content-Type" to "application/xacml+json",
        "Autorization" to Base64.getEncoder().encodeToString("Basic ${fasitProperties.abacUser}:${fasitProperties.abacPass}".toByteArray())
)

private fun evaluate(xacmlRequestBuilder: XacmlRequestBuilder): XacmlResponseWrapper {
    val xacmlJson = gson.toJson(xacmlRequestBuilder.build())
    val result = khttp.post(url, headers = ABAC_PDP_HEADERS, data = xacmlJson)
    if(result.statusCode != 200) {
        throw RuntimeException("ABAC call failed with ${result.statusCode}: ${result.text}")
    }
    return XacmlResponseWrapper(result.text)
}