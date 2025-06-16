import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        vendor.set(JvmVendorSpec.AMAZON)
    }

    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        binaries {
            // Configures a JavaExec task named "runJvm" and a Gradle distribution for the "main" compilation in this target
            executable {
                mainClass.set("com.example.proxy.MainKt")
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs {
            binaries.executable()
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.ktor.network)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        jvmMain {}

        jvmTest {}

        wasmJsMain {}
        wasmJsTest {}
    }
}

// Add Ktor EAP repository for WASM artifacts
repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
}