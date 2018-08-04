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
#-ignorewarnings
-dontpreverify
-verbose
-printmapping proguardMapping.txt
-dontusemixedcaseclassnames
-useuniqueclassmembernames
-keep class butterknife.** { *; }
-keep class butterknife.compiler.** {*;}
-dontwarn butterknife.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-dontwarn com.google.**
-keep class com.google.** { *; }
-dontwarn com.squareup.javapoet.**
-keep class com.squareup.javapoet.** { *; }
-dontwarn com.tencent.bugly.**
-keep class com.tencent.bugly.** { *; }
-dontwarn com.trello.rxlifecycle2.**
-keep,includedescriptorclasses class com.trello.rxlifecycle2.** { *; }
-dontwarn com.umeng.socialize.**
-keep class com.umeng.socialize.** { *; }
-dontwarn okio.**
-keep class okio.**  { *; }
-dontwarn com.blankj.**
-keep class com.blankj.**  { *; }
-dontwarn com.dl7.player.**
-keep,includedescriptorclasses class com.dl7.player.**  { *; }
-dontwarn com.umeng.**
-keep,includedescriptorclasses class com.umeng.**  { *; }
-dontwarn okhttp3.**
-keep,includedescriptorclasses class okhttp3.**  { *; }
# keep framework class
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.app.backup.BackupAgent
-keep public class com.android.vending.licensing.ILicensingService
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
-keep,includedescriptorclasses public class * extends android.view.View {
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# keep Parcelable CREATOR members
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keepattributes Signature,*Annotation*,InnerClasses,RuntimeVisibleAnnotations,AnnotationDefault
-keep class **.R$* {
    *;
}
-keepclassmembers class * {
    void *(**On*Event);
}
-dontwarn retrofit2.Platform$Java8
-keep class retrofit2.Platform$Java8**
-keep,includedescriptorclasses class com.squareup.retrofit2.** {*;}
-dontwarn com.squareup.retrofit2.**
-keep,includedescriptorclasses class retrofit2.** {*;}

-keepclasseswithmembernames class com.xyoye.dandanplay.bean.** {*;}

-keepclasseswithmembernames class com.xyoye.dandanplay.bean.BannerBeans {*;}

-keepattributes *Annotation*

-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
 
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
-keep class tv.danmaku.ijk.media.player.** {*;}
-keep class tv.danmaku.ijk.media.player.IjkMediaPlayer{*;}
-keep class tv.danmaku.ijk.media.player.ffmpeg.FFmpegApi{*;}
-keep class wseemann.media**{*;}