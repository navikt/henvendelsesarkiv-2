package no.nav.henvendelsesarkiv.abac

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName

private val gson = GsonBuilder().create()

enum class Decision {
    Permit,
    Deny,
    NotApplicable,
    Indeterminate;
}

private data class AbacResponse (
        @SerializedName("Response") val response: List<Result>
)

private data class Result(
        @SerializedName("Decision") val decision: Decision,
        @SerializedName("Status") val status: Status?,
        @SerializedName("Obligations") val obligations: List<ObligationOrAdvice>?,
        @SerializedName("AssociatedAdvice") val associatedAdvice: List<ObligationOrAdvice>?,
        @SerializedName("PolicyIdentifierList") val policyIdentifierList: PolicyIdentifier?
)

private data class Status(
        @SerializedName("StatusCode") val statusCode: StatusCode?
)

private data class StatusCode(
        @SerializedName("Value") val value: String
)

private data class ObligationOrAdvice(
        @SerializedName("Id") val id: String,
        @SerializedName("AttributeAssignment") val attributeAssignment: List<AttributeAssignment>?
)

private data class AttributeAssignment(
        @SerializedName("AttributeId") val attributeId: String,
        @SerializedName("Value") val value: String,
        @SerializedName("Issuer") val issuer: String?,
        @SerializedName("DataType") val dataType: String?,
        @SerializedName("Category") val category: String?
)

private data class PolicyIdentifier(
        @SerializedName("PolicyIdReference") val policyIdReference: List<IdReference>?,
        @SerializedName("PolicySetIdReference") val policySetIdReference: List<IdReference>?
)

private data class IdReference(
        @SerializedName("Id") val id: String,
        @SerializedName("Version") val version: String?
)

class XacmlResponseWrapper(xacmlResponse: String) {
    private var result: Result

    init {
        val results: AbacResponse = gson.fromJson(xacmlResponse, AbacResponse::class.java)
        if(results.response.isNotEmpty()) {
            result = results.response[0]
        } else {
            throw RuntimeException("Empty response")
        }
    }

    fun getDecision(): Decision = result.decision

    fun getStatusLogLine(): String = "ABAC ansvered with status ${result.status?.statusCode?.value}"

    fun getNumberOfObligations(): Int = result.obligations?.size ?: 0

    fun getOblogationsLogLine(): String = "ABAC answered with ${result.obligations?.size} obligations"

    fun getNumberOfAdvice(): Int = result.associatedAdvice?.size ?: 0

    fun getAdviceLogLine(): String = "ABAC answered with ${result.associatedAdvice?.size} advice"
}