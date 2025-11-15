plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
    // TODO: Add protobuf plugin when implementing Proto DataStore
//     id("com.google.protobuf") version "0.9.4"
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.datastore.proto"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    // TODO: Add when implementing Proto DataStore
    // implementation(libs.datastore)
    // implementation(libs.protobuf.javalite)
    implementation(libs.kotlinx.coroutines.core)
}

// TODO: Configure protobuf code generation when implementing Proto DataStore
/*
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.0"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
*/
