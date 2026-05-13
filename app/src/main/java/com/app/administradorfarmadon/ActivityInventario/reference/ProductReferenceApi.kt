package com.app.administradorfarmadon.ActivityInventario.reference

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RxNormApi {

    @GET("rxcui.json")
    suspend fun findRxcuiByName(
        @Query("name") name: String
    ): RxCuiResponse

    @GET("approximateTerm.json")
    suspend fun approximateTerm(
        @Query("term") term: String,
        @Query("maxEntries") maxEntries: Int = 5
    ): RxApproximateResponse

    @GET("rxcui/{rxcui}/properties.json")
    suspend fun getProperties(
        @Path("rxcui") rxcui: String
    ): RxPropertiesResponse
}

interface MedlinePlusApi {

    @GET("service")
    suspend fun getDrugInfoByRxcui(
        @Query("mainSearchCriteria.v.cs") codeSystem: String = "2.16.840.1.113883.6.88",
        @Query("mainSearchCriteria.v.c") rxcui: String,
        @Query("informationRecipient.languageCode.c") language: String = "es",
        @Query("knowledgeResponseType") responseType: String = "application/json"
    ): MedlinePlusResponse

    @GET("service")
    suspend fun getDrugInfoByNdc(
        @Query("mainSearchCriteria.v.cs") codeSystem: String = "2.16.840.1.113883.6.69",
        @Query("mainSearchCriteria.v.c") ndc: String,
        @Query("informationRecipient.languageCode.c") language: String = "es",
        @Query("knowledgeResponseType") responseType: String = "application/json"
    ): MedlinePlusResponse
}

data class RxCuiResponse(
    val idGroup: RxIdGroup?
)

data class RxIdGroup(
    val name: String?,
    val rxnormId: List<String>?
)

data class RxApproximateResponse(
    val approximateGroup: RxApproximateGroup?
)

data class RxApproximateGroup(
    val inputTerm: String?,
    val candidate: List<RxCandidate>?
)

data class RxCandidate(
    val rxcui: String?,
    val rxaui: String?,
    val score: String?,
    val rank: String?,
    val name: String? = null,
    val source: String? = null
)

data class RxPropertiesResponse(
    val properties: RxProperties?
)

data class RxProperties(
    val rxcui: String?,
    val name: String?,
    val synonym: String?,
    val tty: String?,
    val language: String?,
    val suppress: String?,
    val umlscui: String?
)

data class MedlinePlusResponse(
    val feed: MedlineFeed?
)

data class MedlineFeed(
    val title: MedlineText?,
    val entry: List<MedlineEntry>?
)

data class MedlineEntry(
    val title: MedlineText?,
    val link: List<MedlineLink>?,
    val summary: MedlineText?,
    val author: List<MedlineAuthor>? = null
)

data class MedlineText(
    @Json(name = "_value") val rawValue: String? = null,
    val value: String? = null,
    val type: String? = null
) {
    fun resolved(): String? = value ?: rawValue
}

data class MedlineLink(
    val href: String?,
    val rel: String? = null
)

data class MedlineAuthor(
    val name: MedlineText?
)
