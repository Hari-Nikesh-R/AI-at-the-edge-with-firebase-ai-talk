package com.agenticedge.shopdemo.data.event

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

/**
 * Local category-affinity cache. Lets the app produce recommendations with zero
 * network access, demonstrating the README's "Offline Recommendations" capability.
 */
@Entity(tableName = "product_preferences")
data class ProductPreference(
    @PrimaryKey val category: String,
    val viewCount: Int,
    val lastViewedMs: Long
)

@Dao
interface ProductPreferenceDao {

    @Query("SELECT * FROM product_preferences WHERE category = :category")
    suspend fun get(category: String): ProductPreference?

    @Upsert
    suspend fun upsert(preference: ProductPreference)

    @Query("SELECT * FROM product_preferences ORDER BY viewCount DESC")
    suspend fun topCategories(): List<ProductPreference>

    suspend fun recordView(category: String, timestampMs: Long) {
        val existing = get(category)
        upsert(
            ProductPreference(
                category = category,
                viewCount = (existing?.viewCount ?: 0) + 1,
                lastViewedMs = timestampMs
            )
        )
    }
}
