# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Aggressive optimization for minimal APK size
-optimizationpasses 5
-dontpreverify
-repackageclasses ''
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively

# Enable all safe optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Remove unused code aggressively
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Additional debug code removal
-assumenosideeffects class java.lang.System {
    public static void out.println(...);
    public static void err.println(...);
}

# Enhanced code optimization
-optimizations !code/simplification/string
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# More aggressive class merging and inlining
-allowaccessmodification
-flattenpackagehierarchy

# Remove more unused code
-assumenosideeffects class * {
    @android.support.annotation.Keep *;
}

# Keep only essential classes
-keep class io.github.sankalp.lunox.LauncherActivity { *; }
-keep class io.github.sankalp.lunox.** { *; }

# Remove debug information but keep essential attributes for crash reports
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,*Annotation*

# Remove unused resources more aggressively
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Optimize string constants
-optimizations !code/simplification/string,!field/*,!class/merging/*