-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class com.thormakk.keyarmor.** { @javax.inject.Inject <init>(...); }
-keep class com.thormakk.keyarmor.** { @javax.inject.Inject <fields>; }
-keepclassmembers class com.thormakk.keyarmor.** {
    @javax.inject.Inject <init>(...);
}
-dontwarn dagger.hilt.**

-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.TypeConverter class * { *; }
-dontwarn androidx.room.**

-keep class com.thormakk.keyarmor.** extends androidx.work.Worker
-keep class com.thormakk.keyarmor.** extends androidx.work.CoroutineWorker
-keep class com.thormakk.keyarmor.** extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep @androidx.hilt.work.HiltWorker class * { *; }
-dontwarn androidx.work.**

-dontwarn kotlinx.coroutines.**

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.auth.oauth2.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.apis.**

-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn com.google.gson.**

-dontwarn okhttp3.**
-dontwarn okio.**

-dontwarn com.gemalto.jp2.**

-keep class com.google.zxing.** { *; }
-dontwarn com.journeyapps.**
-dontwarn com.google.zxing.**

-dontwarn androidx.compose.**

-dontwarn org.apache.http.**
-dontwarn android.net.http.**
-dontwarn javax.naming.**
-dontwarn javax.servlet.**
-dontwarn org.apache.avalon.framework.**
-dontwarn org.apache.log.**
-dontwarn org.apache.log4j.**
-dontwarn org.ietf.jgss.**

-dontwarn com.revenuecat.purchases.**
-dontwarn org.joda.time.Instant

-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**

-keep class com.thormakk.keyarmor.widget.KeyArmorWidget { *; }
-keep class com.thormakk.keyarmor.widget.KeyArmorWidgetReceiver { *; }
-keep class com.thormakk.keyarmor.widget.KeyArmorWidgetUpdater { *; }
-keep class * extends androidx.glance.appwidget.action.ActionCallback { *; }
-dontwarn androidx.glance.**

-keep class com.thormakk.keyarmor.passkey.KeyArmorCredentialProviderService { *; }
-keep class com.thormakk.keyarmor.passkey.PasskeyCreationActivity { *; }
-keep class com.thormakk.keyarmor.passkey.PasskeyGetActivity { *; }
-keep class com.thormakk.keyarmor.data.passkey.PasskeyCrypto { *; }
-keep class com.thormakk.keyarmor.data.passkey.CborUtils { *; }
-keep @dagger.hilt.EntryPoint interface * { *; }

-keepclassmembers enum com.thormakk.keyarmor.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public java.lang.String name();
}

-keep class com.goterl.lazysodium.** { *; }
-keep class com.sun.jna.** { *; }
-keep class * extends com.sun.jna.Structure { *; }
-keepclassmembers class * extends com.sun.jna.Structure { *; }
-dontwarn java.awt.**

-keep class org.bouncycastle.crypto.generators.Argon2BytesGenerator { *; }
-keep class org.bouncycastle.crypto.params.Argon2Parameters { *; }
-keep class org.bouncycastle.crypto.params.Argon2Parameters$Builder { *; }
