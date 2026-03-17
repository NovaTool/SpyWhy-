# SpyWhy Wallet ProGuard Rules

# Keep Moshi JSON adapters
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Keep Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Keep crypto classes
-keep class com.spywhy.wallet.core.crypto.** { *; }

# Keep bitcoinj
-keep class org.bitcoinj.** { *; }
-dontwarn org.bitcoinj.**

# Keep web3j
-keep class org.web3j.** { *; }
-dontwarn org.web3j.**

# Keep ONNX Runtime
-keep class ai.onnxruntime.** { *; }

# General
-dontwarn javax.annotation.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
