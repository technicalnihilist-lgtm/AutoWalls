-keep class com.autowalls.app.data.remote.dto.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
