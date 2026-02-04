import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.xyoye.dandanplay.buildlogic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(androidx.gradle.plugin)
    compileOnly(kotlinx.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("dandanplayApplicationPlugin") {
            id = "dandanplay.android.application"
            version = "1.0.0"
            implementationClass = "AndroidApplicationPlugin"
        }
        register("dandanplayLibraryPlugin") {
            id = "dandanplay.android.library"
            version = "1.0.0"
            implementationClass = "AndroidLibraryPlugin"
        }
        register("dandanplayRouterPlugin") {
            id = "dandanplay.android.router"
            version = "1.0.0"
            implementationClass = "AndroidRouterPlugin"
        }
    }
}
