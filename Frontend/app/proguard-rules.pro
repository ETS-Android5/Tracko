-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-dontwarn **CompatHoneycomb
-dontwarn android.support.v4.**
-dontwarn com.google.common.cache.**
-dontwarn com.google.common.primitives.**
-dontwarn androidx.appcompat.widget.**

-keep class android.widget.** {*; }
-dontwarn android.widget.**

-keep class androidx.appcompat.widget.** { *; }
-dontwarn androidx.appcompat.widget.**

-keep class android.graphics.** { *; }
-dontwarn android.graphics.**

-keep class androidx.core.graphics.** { *; }
-dontwarn androidx.core.graphics.**

-dontwarn org.xmlpull.v1.**
-dontwarn org.**
-dontwarn com.google.android.gms.**

-dontwarn org.simpleframework.**
-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.spongycastle.**
-dontwarn org.spongycastle.x509.**
-dontwarn org.spongycastle.x509.extension.**
-dontwarn org.spongycastle.x509.util.**
-dontwarn org.w3c.**
-ignorewarnings

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep class android.support.v4.** { *; }
-keep class org.xmlpull.** { *; }
-keep class org.** { *; }
-keep class com.google.android.gms.** { *; }

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keepclasseswithmembers class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep class sun.misc.Unsafe { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# (1)Annotations and signatures
-keepattributes *Annotation*
-keepattributes Signature

# (2) XSD
-keep public class com.kaavya.xsd.**{ *; }

#(3) Firebase
-keep class com.google.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.apache.**
-dontwarn org.w3c.dom.**

