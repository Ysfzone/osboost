# Keep MainActivity & FileProvider / ShizukuProvider entry points
-keep class com.ysfzone.goclean.MainActivity { *; }
-keep class rikka.shizuku.ShizukuProvider { *; }
-keep class androidx.core.content.FileProvider { *; }

# Shizuku Binder interfaces
-keep class moe.shizuku.server.** { *; }
-keep class rikka.shizuku.** { *; }
-dontwarn rikka.shizuku.**

# Strip verbose debug logs in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

