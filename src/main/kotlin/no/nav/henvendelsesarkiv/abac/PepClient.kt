package no.nav.henvendelsesarkiv.abac

import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import kotlinx.coroutines.runBlocking
import no.nav.henvendelsesarkiv.fasitProperties
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("henvendelsesarkiv.PepClient")
private val url = fasitProperties.abacEndpoint
private val gson = GsonBuilder().setPrettyPrinting().create()

private const val PEP_ID = "henvendelsesarkiv"
private const val DOMENE = "brukerdialog"

class PepClient(private val bias: Decision, private val httpClient: HttpClient) {

    fun checkAccess(bearerToken: String?, action: String): Boolean {
        requireNotNull(bearerToken) { "Authorization token not set" }
        val token = bearerToken.substringAfter(" ")
        return hasAccessToResource(extractBodyFromOidcToken(token), action)
    }

    private fun hasAccessToResource(oidcTokenBody: String, action: String): Boolean {
        val response = evaluate(createRequestWithDefaultHeaders(oidcTokenBody, action))
        logAnswer(response)
        return createBiasedDecision(response.getDecision()) == Decision.Permit
    }

    private fun evaluate(xacmlRequestBuilder: XacmlRequestBuilder): XacmlResponseWrapper {
        val xacmlJson = gson.toJson(xacmlRequestBuilder.build())
        log.info(xacmlJson)

        return runBlocking {
            val result = httpClient.post<HttpResponse>(url) {
                body = xacmlJson
                header("Content-Type", "application/xacml+json")
            }
            val resultText = result.readText()
            if (result.status.value != 200) {
                throw RuntimeException(
                        "ABAC call failed with statuscode: ${result.status.value}, resultText: $resultText"
                )
            }
            log.info(resultText)
            XacmlResponseWrapper(resultText)
        }
    }

    private fun createRequestWithDefaultHeaders(oidcTokenBody: String, action: String): XacmlRequestBuilder {
        return XacmlRequestBuilder()
                .addEnvironmentAttribute(ENVIRONMENT_OIDC_TOKEN_BODY, oidcTokenBody)
                .addEnvironmentAttribute(ENVIRONMENT_PEP_ID, PEP_ID)
                .addResourceAttribute(RESOURCE_DOMENE, DOMENE)
                .addActionAttribute(ACTION_ID, action)
    }

    private fun createBiasedDecision(decision: Decision): Decision {
        return when (decision) {
            Decision.NotApplicable, Decision.Indeterminate -> bias
            else -> decision
        }
    }

    private fun logAnswer(response: XacmlResponseWrapper) {
        log.debug(response.getStatusLogLine())
        if (response.getNumberOfObligations() > 0) log.info(response.getOblogationsLogLine())
        if (response.getNumberOfAdvice() > 0) log.info(response.getAdviceLogLine())
    }

    private fun extractBodyFromOidcToken(token: String): String {
        return token.substringAfter(".").substringBefore(".")
    }
}