# ==============================================================================
# ProGuard / R8 Rules untuk MyBBM (Release APK)
# Menjaga kompatibilitas penuh untuk Room, Supabase, Kotlinx Serialization,
# Ktor, OkHttp, dan Osmdroid.
# ==============================================================================

# Pertahankan atribut anotasi, generik, dan inner classes
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# ------------------------------------------------------------------------------
# 1. Kotlinx Serialization (@Serializable, Companion, $$serializer)
# ------------------------------------------------------------------------------
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mybbm.app.**$$serializer { *; }
-keepclassmembers class com.mybbm.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.mybbm.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class ** { *; }
-keep class com.mybbm.app.data.remote.supabase.** { *; }
-keep class com.mybbm.app.data.local.seed.** { *; }

# ------------------------------------------------------------------------------
# 2. Room Database, Entities, DAOs, dan Converters
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.mybbm.app.data.local.** { *; }

# ------------------------------------------------------------------------------
# 3. Supabase Kotlin SDK, Ktor Client, & OkHttp
# ------------------------------------------------------------------------------
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn io.github.jan.supabase.**
-dontwarn org.slf4j.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn java.lang.invoke.**

# ------------------------------------------------------------------------------
# 4. OpenStreetMap (Osmdroid)
# ------------------------------------------------------------------------------
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# ------------------------------------------------------------------------------
# 5. Kotlin Coroutines
# ------------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
