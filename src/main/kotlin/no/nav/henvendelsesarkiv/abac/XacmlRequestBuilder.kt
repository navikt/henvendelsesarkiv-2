package no.nav.henvendelsesarkiv.abac

import com.google.gson.annotations.SerializedName

enum class Category {
    Resource,
    Action,
    Environment,
    AccessSubject,
    RecipientSubject,
    IntermediarySubject,
    Codebase,
    RequestingMachine;
}

data class CategoryAttribute(
    @SerializedName("Attribute") var attributes: List<CAttribute> = ArrayList()
)

data class CAttribute(
        @SerializedName("AttributeId") val attributeId: String,
        @SerializedName("Value") val value: Any
        )

class XacmlRequestBuilder() {
    private val requestAttributes = HashMap<Category, CategoryAttribute>()

    fun addResourceAttribute(id: String, value: Any): XacmlRequestBuilder {
        addAttributeToCategory(Category.Resource, id, value)
        return this
    }

    fun build(): Map<String, Map<Category, CategoryAttribute>> {
        return mapOf("Request" to requestAttributes)
    }

    private fun addAttributeToCategory(category: Category, id: String, value: Any) {
        requestAttributes.getOrPut(category) { CategoryAttribute() }.attributes += CAttribute(id, value)
    }
}