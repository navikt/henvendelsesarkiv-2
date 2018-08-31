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

data class AbacResponse (
        @SerializedName("Response") val response: List<Result>
)

data class Result(
        @SerializedName("Decision") val decision: Decision,
        @SerializedName("Status") val status: Status?,
        @SerializedName("Obligations") val obligations: List<ObligationOrAdvice>?,
        @SerializedName("AssociatedAdvice") val associatedAdvice: List<ObligationOrAdvice>?,
        @SerializedName("PolicyIdentifierList") val policyIdentifierList: PolicyIdentifier?
)

data class Status(
        @SerializedName("StatusCode") val statusCode: StatusCode?
)

data class StatusCode(
        @SerializedName("Value") val value: String
)

data class ObligationOrAdvice(
        @SerializedName("Id") val id: String,
        @SerializedName("AttributeAssignment") val attributeAssignment: List<AttributeAssignment>?
)

data class AttributeAssignment(
        @SerializedName("AttributeId") val attributeId: String,
        @SerializedName("Value") val value: String,
        @SerializedName("Issuer") val issuer: String?,
        @SerializedName("DataType") val dataType: String?,
        @SerializedName("Category") val category: String?
)

data class PolicyIdentifier(
        @SerializedName("PolicyIdReference") val policyIdReference: List<IdReference>?,
        @SerializedName("PolicySetIdReference") val policySetIdReference: List<IdReference>?
)

data class IdReference(
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
}