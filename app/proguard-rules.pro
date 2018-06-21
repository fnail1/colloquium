# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn org.apache.**

# https://docs.fabric.io/android/crashlytics/dex-and-proguard.html
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
#-keepresourcexmlelements manifest/application/meta-data@name=io.fabric.ApiKey

# Crashlytics
-printmapping mapping.txt
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-keep class com.crashlytics.android.**

# Retrofit
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okhttp3.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Guava
-keep class com.google.j2objc.annotations.** { *; }
-dontwarn   com.google.j2objc.annotations.**
-keep class java.lang.ClassValue { *; }
-dontwarn   java.lang.ClassValue
-dontwarn com.google.errorprone.annotations.**
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**

-keepattributes app.laiki.toolkit.data.DbTable
-keepattributes app.laiki.toolkit.data.DbColumn

-keepnames @app.laiki.toolkit.data.DbTable class *
-keepnames class app.laiki.api.model.** {*;}
-keepnames class app.laiki.toolkit.data.** {*;}
-keepnames class app.laiki.model.entities.** {*;}
-keepnames class app.laiki.model.types.** {*;}


-dontwarn com.android.installreferrer

-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**

-keep class android.support.v7.widget.SearchView { *; }

