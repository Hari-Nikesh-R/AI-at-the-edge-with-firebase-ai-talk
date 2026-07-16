package com.agenticedge.shopdemo.data.model

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Int,
    val discountPercent: Int,
    val rating: Float,
    val reviewCount: Int,
    val emoji: String,
    val specs: List<String>,
    val reviews: List<String>
) {
    val discountedPrice: Int get() = price - (price * discountPercent / 100)
}

object ProductCatalog {

    val all: List<Product> = listOf(
        Product(
            id = "phone-1",
            name = "Pulse X13 Pro",
            category = "Phones",
            price = 48999,
            discountPercent = 12,
            rating = 4.5f,
            reviewCount = 2341,
            emoji = "📱",
            specs = listOf(
                "6.5\" OLED, 120Hz",
                "50MP triple camera",
                "5000mAh battery, 65W charging",
                "12GB RAM / 256GB storage"
            ),
            reviews = listOf(
                "Battery easily lasts a full day of heavy use.",
                "Camera in low light is a big step up from my last phone.",
                "Feels premium, screen is excellent for video."
            )
        ),
        Product(
            id = "phone-2",
            name = "Nova Lite 5G",
            category = "Phones",
            price = 21999,
            discountPercent = 18,
            rating = 4.1f,
            reviewCount = 987,
            emoji = "📱",
            specs = listOf(
                "6.1\" LCD, 90Hz",
                "48MP dual camera",
                "4500mAh battery",
                "8GB RAM / 128GB storage"
            ),
            reviews = listOf(
                "Great value for the price.",
                "Good enough for everyday browsing and calls."
            )
        ),
        Product(
            id = "laptop-1",
            name = "Aeroblade 14",
            category = "Laptops",
            price = 89999,
            discountPercent = 8,
            rating = 4.6f,
            reviewCount = 512,
            emoji = "💻",
            specs = listOf(
                "14\" 2.8K OLED",
                "12-core edge AI processor",
                "16GB RAM / 1TB SSD",
                "18-hour battery life"
            ),
            reviews = listOf(
                "Silent even under heavy load, fantastic keyboard.",
                "Runs on-device AI models noticeably faster than my old laptop."
            )
        ),
        Product(
            id = "laptop-2",
            name = "WorkBook Air",
            category = "Laptops",
            price = 54999,
            discountPercent = 15,
            rating = 4.2f,
            reviewCount = 764,
            emoji = "💻",
            specs = listOf(
                "13.3\" IPS",
                "8-core processor",
                "8GB RAM / 512GB SSD",
                "10-hour battery life"
            ),
            reviews = listOf(
                "Light enough to carry everywhere.",
                "Handles spreadsheets and browser tabs without lag."
            )
        ),
        Product(
            id = "headphone-1",
            name = "SonicWave ANC",
            category = "Audio",
            price = 8999,
            discountPercent = 25,
            rating = 4.4f,
            reviewCount = 3120,
            emoji = "🎧",
            specs = listOf(
                "Active noise cancellation",
                "40-hour battery",
                "Bluetooth 5.3",
                "On-device wake word detection"
            ),
            reviews = listOf(
                "ANC blocks out office noise completely.",
                "Battery lasts the whole work week for me."
            )
        ),
        Product(
            id = "headphone-2",
            name = "EchoBuds Mini",
            category = "Audio",
            price = 2999,
            discountPercent = 10,
            rating = 3.9f,
            reviewCount = 640,
            emoji = "🎧",
            specs = listOf(
                "Passive noise isolation",
                "6-hour battery + case",
                "Bluetooth 5.1"
            ),
            reviews = listOf(
                "Solid budget pick for the gym.",
                "Case is compact, fits any pocket."
            )
        ),
        Product(
            id = "watch-1",
            name = "PulseFit Watch 3",
            category = "Wearables",
            price = 14999,
            discountPercent = 20,
            rating = 4.3f,
            reviewCount = 1204,
            emoji = "⌚",
            specs = listOf(
                "On-device heart-rate AI",
                "AMOLED always-on display",
                "7-day battery",
                "Offline workout tracking"
            ),
            reviews = listOf(
                "Sleep tracking is surprisingly accurate.",
                "Great screen, easy to read outdoors."
            )
        ),
        Product(
            id = "tablet-1",
            name = "Slate 11 Plus",
            category = "Tablets",
            price = 34999,
            discountPercent = 14,
            rating = 4.0f,
            reviewCount = 421,
            emoji = "📲",
            specs = listOf(
                "11\" 2K display",
                "Edge AI stylus prediction",
                "8GB RAM / 256GB storage",
                "12-hour battery life"
            ),
            reviews = listOf(
                "Stylus feels natural for note-taking.",
                "Good for streaming and light editing."
            )
        )
    )

    fun byId(id: String): Product? = all.find { it.id == id }

    fun byCategory(category: String): List<Product> = all.filter { it.category == category }

    val categories: List<String> = all.map { it.category }.distinct()
}
