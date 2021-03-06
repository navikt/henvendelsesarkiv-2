package no.nav.henvendelsesarkiv.abac

import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.basic.BasicAuth
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.henvendelsesarkiv.ApplicationProperties

private val gson = GsonBuilder().setPrettyPrinting().create()
private val abacCache = AbacCache()

private const val XACML_CONTENT_TYPE = "application/xacml+json"
private const val PEP_ID = "henvendelsesarkiv"
private const val DOMENE = "brukerdialog"

class PepClient(private val applicationProperties: ApplicationProperties, private val bias: Decision) {
    private val url = applicationProperties.abacEndpoint
    private val abacClient = HttpClient(Apache) {
        install(BasicAuth) {
            username = applicationProperties.abacUser
            password = applicationProperties.abacPass
        }
    }

    suspend fun checkAccess(bearerToken: String?, method: String, action: String): Boolean {
        requireNotNull(bearerToken) { "Authorization token not set" }
        val token = bearerToken.substringAfter(" ")
        return hasAccessToResource(extractBodyFromOidcToken(token), method, action)
    }

    private suspend fun hasAccessToResource(oidcTokenBody: String, method: String, action: String): Boolean {
        val cachedResponse = abacCache.hasAccess(oidcTokenBody, method, action)
        if (cachedResponse != null) {
            return cachedResponse
        }

        val response = evaluate(createRequestWithDefaultHeaders(oidcTokenBody, action))
        val decision = createBiasedDecision(response.getDecision()) == Decision.Permit
        abacCache.storeResultOfLookup(oidcTokenBody, method, action, decision)
        return decision
    }

    private suspend fun evaluate(xacmlRequestBuilder: XacmlRequestBuilder): XacmlResponseWrapper {
        val xacmlJson = gson.toJson(xacmlRequestBuilder.build())
        return withContext(Dispatchers.IO) {
            val result = abacClient.post<HttpResponse>(url) {
                body = TextContent(xacmlJson, ContentType.parse(XACML_CONTENT_TYPE))
            }
            if (result.status.value != 200) {
                throw RuntimeException("ABAC call failed with ${result.status.value}")
            }
            val res = result.readText()
            XacmlResponseWrapper(res)
        }
    }

    private fun createRequestWithDefaultHeaders(oidcTokenBody: String, action: String): XacmlRequestBuilder =
        XacmlRequestBuilder()
            .addEnvironmentAttribute(ENVIRONMENT_OIDC_TOKEN_BODY, oidcTokenBody)
            .addEnvironmentAttribute(ENVIRONMENT_PEP_ID, PEP_ID)
            .addResourceAttribute(RESOURCE_DOMENE, DOMENE)
            .addActionAttribute(ACTION_ID, action)

    private fun createBiasedDecision(decision: Decision): Decision =
        when (decision) {
            Decision.NotApplicable, Decision.Indeterminate -> bias
            else -> decision
        }

    private fun extractBodyFromOidcToken(token: String): String =
        token.substringAfter(".").substringBefore(".")
}
