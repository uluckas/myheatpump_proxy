plugins {
    kotlin("multiplatform") version "2.1.20"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    jvm {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        binaries {
            // Configures a JavaExec task named "runJvm" and a Gradle distribution for the "main" compilation in this target
            executable {
                mainClass.set("foo.MainKt")
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        nodejs {
            binaries.executable()
            testTask {
                useMocha {
                    timeout = "30s"
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-server-cio:2.3.7")
                implementation("io.ktor:ktor-network:2.3.7")
                implementation("io.ktor:ktor-network-tls:2.3.7")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting

        val wasmJsMain by getting
        val wasmJsTest by getting
    }
}
