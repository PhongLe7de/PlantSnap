# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============ Kotlinx Serialization ============
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.plantsnap.**$$serializer { *; }
-keepclassmembers class com.plantsnap.** {
    *** Companion;
}
-keepclasseswithmembers class com.plantsnap.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.plantsnap.data.remote.** { *; }
-keep class com.plantsnap.domain.models.** { *; }

# ============ Retrofit ============
-keepattributes Signature, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class com.plantsnap.data.plantnet.** { *; }

# ============ OkHttp ============
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# ============ Supabase / Ktor ============
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ============ Room ============
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ============ Google GenAI ============
-keep class com.google.genai.** { *; }
-dontwarn com.google.genai.**

# ============ Compose ============
-dontwarn androidx.compose.**