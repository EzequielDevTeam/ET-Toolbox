# ProGuard rules for ET Toolbox

# Keep libsu classes
-keep class com.topjohnwu.superuser.** { *; }

# Keep Material3 components
-keep class com.google.android.material.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep ViewBinding generated classes
-keep class technology.ezequieldevteam.ettoolbox.databinding.** { *; }

# Keep R8/ProGuard generated BuildConfig
-keep class technology.ezequieldevteam.ettoolbox.BuildConfig { *; }

# Keep enum values for serialization
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Room database (if added later)
# -keep class * extends androidx.room.RoomDatabase { *; }

# Keep Service/Activity/Fragment/BroadcastReceiver/ContentProvider
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
}

# Optimize
-optimizationpasses 5
-allowaccessmodification

# Don't warn about missing classes
-dontwarn com.topjohnwu.superuser.**
-dontwarn kotlinx.coroutines.**