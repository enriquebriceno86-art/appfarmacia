package com.app.administradorfarmadon.ActivityInventario.reference

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class ProductReferenceLocalCache(
    context: Context
) {
    private val prefs = context.getSharedPreferences("product_reference_cache", Context.MODE_PRIVATE)
    private val adapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(ProductReferenceEntity::class.java)

    suspend fun findByNormalizedName(normalizedName: String): ProductReferenceEntity? {
        val raw = prefs.getString(normalizedName, null) ?: return null
        return runCatching { adapter.fromJson(raw) }.getOrNull()
    }

    suspend fun upsert(entity: ProductReferenceEntity) {
        val json = adapter.toJson(entity)
        prefs.edit().putString(entity.normalizedName, json).apply()
    }
}
