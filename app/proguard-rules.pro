# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:/Users/Rocky/AppData/Local/Android/android-studio/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
-keep public class com.sails.engine.patterns.IconPatterns
-dontwarn org.apache.**
-keep class org.kobjects.** {*;}
-keep class org.ksoap2.** {*;}
-keep class org.kxml2.** {*;}
-dontwarn org.xmlpull.v1.**
-keep class org.xmlpull.v1.** {*;}
# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
