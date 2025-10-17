# ================================================================================================
# :feature:recording Module - ProGuard Rules
# ================================================================================================
# This module provides recording functionality using Stream Video SDK (WebRTC) and CameraX.
# These rules ensure video/audio streaming and camera operations work correctly after R8.

# ================================================================================================
# Stream Video SDK 1.13.0 (WebRTC)
# ================================================================================================
# WebRTC uses JNI and reflection - keep native method signatures

-keep class io.getstream.video.** { *; }
-keep class org.webrtc.** { *; }

# Keep native methods (required for JNI)
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep WebRTC data channel and peer connection classes
-keep class org.webrtc.DataChannel { *; }
-keep class org.webrtc.PeerConnection { *; }
-keep class org.webrtc.PeerConnectionFactory { *; }
-keep class org.webrtc.VideoTrack { *; }
-keep class org.webrtc.AudioTrack { *; }

# ================================================================================================
# CameraX 1.5.1
# ================================================================================================
# CameraX uses reflection for camera provider

-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
-keep class androidx.camera.lifecycle.** { *; }
-keep class androidx.camera.view.** { *; }
-keep class androidx.camera.video.** { *; }

# ================================================================================================
# WorkManager 2.10+ (if used for background recording tasks)
# ================================================================================================
# Keep Worker classes that are instantiated via reflection

-keep public class * extends androidx.work.Worker
-keep public class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep WorkManager's DefaultWorkerFactory
-keep class androidx.work.impl.WorkManagerInitializer { *; }
