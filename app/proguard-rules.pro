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

# Gson 2.10+: mantener la firma genérica de los TypeToken anónimos. AppPreferences usa
# object : TypeToken<Set<Int>>() {} y TypeToken<List<Int>>() {}; sin estas reglas R8
# strippea el generic y Gson tira "TypeToken must be created with a type argument".
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

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
