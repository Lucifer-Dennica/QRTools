# Основные классы приложения
-keep class com.luciferdennica.qrtools.** { *; }
-dontwarn kotlinx.**

# Яндекс РСЯ (обязательно для работы рекламы в release)
-keep class com.yandex.mobile.ads.** { *; }
-dontwarn com.yandex.mobile.ads.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ZXing
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
