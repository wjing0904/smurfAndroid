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

#极光登陆
         -dontoptimize
         -dontpreverify

         -dontwarn cn.jpush.**
         -keep class cn.jpush.** { *; }
         -dontwarn cn.jiguang.**
         -keep class cn.jiguang.** { *; }

         -dontwarn cn.com.chinatelecom.**
         -keep class cn.com.chinatelecom.** { *; }
         -dontwarn com.ct.**
         -keep class com.ct.** { *; }
         -dontwarn a.a.**
         -keep class a.a.** { *; }
         -dontwarn com.cmic.**
         -keep class com.cmic.** { *; }
         -dontwarn com.unicom.**
         -keep class com.unicom.** { *; }
         -dontwarn com.sdk.**
         -keep class com.sdk.** { *; }

         -dontwarn com.sdk.**
         -keep class com.sdk.** { *; }



         -ignorewarnings

         -dontoptimize
         -dontwarn android.app.**
         -dontwarn android.support.**
         -dontwarn sun.misc.**
         -keepattributes Signature
         -keepattributes *Annotation*
         -dontwarn android.support.**
         -dontwarn com.adhub.ads.**
         -dontwarn com.hubcloud.adhubsdk.**

         -keep class sun.misc.**{*;}
         -keep class android.support.** { *; }
         -keep class android.app.**{*;}
         -keep class **.R$* {*;}

         -keep class com.adhub.ads.** {*; }

         -keep class com.hubcloud.adhubsdk.** {*; }

         -keep class com.falcon.adpoymer.** { *; }

         -keep class com.qq.e.** {
             public protected *;
         }

         -keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

         -dontwarn  org.apache.**

         -keep class com.baidu.mobads.** { *; }
         -keep class com.baidu.mobad.** { *; }

         -keep class com.bytedance.sdk.openadsdk.** { *; }
         -keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
         -keep class com.pgl.sys.ces.* {*;}

         -keep class com.inmobi.** { *; }
         -dontwarn com.inmobi.**
         -dontwarn com.squareup.picasso.**
         -keep class com.squareup.picasso.** {*;}
         -dontwarn com.squareup.picasso.**
         -dontwarn com.squareup.okhttp.**
         -keep class com.integralads.avid.library.** {*;}
         -keep class com.bun.** {*;}
         -dontwarn com.bun.**
         -keep class com.iab.** {*;}
         -dontwarn com.iab.**
