package no.nav.henvendelsesarkiv.abac

import com.google.gson.GsonBuilder
import no.nav.henvendelsesarkiv.fasitProperties
import org.slf4j.LoggerFactory
import java.util.*

private val url = fasitProperties.abacEndpoint
private val gson = GsonBuilder().setPrettyPrinting().create()

private val ABAC_PDP_HEADERS = mapOf(
        "Content-Type" to "application/xacml+json",
        "Autorization" to Base64.getEncoder().encodeToString("Basic ${fasitProperties.abacUser}:${fasitProperties.abacPass}".toByteArray())
)

private const val PEP_ID = "henvendelsesarkiv"
private const val DOMENE = "brukerdialog"

class PepClient(private val bias: Decision) {
    private val log = LoggerFactory.getLogger("henvendelsesarkiv.PepClient")

    fun hasAccessToResource(oidcTokenBody: String, action: String): Boolean {
        val response = evaluate(createRequestWithDefaultHeaders(oidcTokenBody, action))
        logAnswer(response)
        return createBiasedDecision(response.getDecision()) == Decision.Permit
    }

    private fun evaluate(xacmlRequestBuilder: XacmlRequestBuilder): XacmlResponseWrapper {
        val xacmlJson = gson.toJson(xacmlRequestBuilder.build())
        val result = khttp.post(url, headers = ABAC_PDP_HEADERS, data = xacmlJson)
        if (result.statusCode != 200) {
            throw RuntimeException("ABAC call failed with ${result.statusCode}: ${result.text}")
        }
        return XacmlResponseWrapper(result.text)
    }

    private fun createRequestWithDefaultHeaders(oidcTokenBody: String, action: String): XacmlRequestBuilder {
        return XacmlRequestBuilder()
                .addEnvironmentAttribute(ENVIRONMENT_OIDC_TOKEN_BODY, oidcTokenBody)
                .addEnvironmentAttribute(ENVIRONMENT_PEP_ID, PEP_ID)
                .addResourceAttribute(RESOURCE_DOMENE, DOMENE)
                .addActionAttribute(ACTION_ID, action)
    }

    private fun createBiasedDecision(decision: Decision): Decision {
        return when(decision) {
            Decision.NotApplicable, Decision.Indeterminate -> bias
            else -> decision
        }
    }

    private fun logAnswer(response: XacmlResponseWrapper) {
        log.debug(response.getStatusLogLine())
        if (response.getNumberOfObligations() > 0) log.info(response.getOblogationsLogLine())
        if (response.getNumberOfAdvice() > 0) log.info(response.getAdviceLogLine())
    }
}