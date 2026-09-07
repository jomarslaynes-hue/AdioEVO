# ProGuard rules for AdioEVO

# Keep all Kotlin classes
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }

# Keep Jetpack Compose
-keep class androidx.compose.** { *; }

# Keep Room Database
-keep class androidx.room.** { *; }
-keepclassmembers class * implements androidx.room.RoomDatabase { *; }

# Keep MIDI related classes
-keep class android.media.midi.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep serialization
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*

# Keep application classes
-keep class com.adioevo.daw.** { *; }

# Keep ViewModel
-keep class androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
