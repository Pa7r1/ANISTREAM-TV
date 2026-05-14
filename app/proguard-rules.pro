# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit

# Gson
-keep class com.google.gson.** { *; }
-keepclassmembers class * { @com.google.gson.annotations.SerializedName <fields>; }

# Data models (needed for Gson deserialization)
-keep class com.anistream.tv.data.model.** { *; }

# API response classes (AniListResponse, ConsumetSearchResponse, etc.)
-keep class com.anistream.tv.data.api.** { *; }

# Keep any class with @SerializedName fields fully intact
-keepclasseswithmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** { **[] $VALUES; public *; }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Leanback
-keep class androidx.leanback.** { *; }
