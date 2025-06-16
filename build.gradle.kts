plugins {
    kotlin("multiplatform") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    linuxArm32Hfp("linuxArm32Hfp") {
        // binaries.executable() // Assuming default behavior or configured elsewhere if needed
        testRuns.maybeCreate("test").enabled = true // Explicitly enable test run
    }

    androidNativeArm32("androidNativeArm32") {
        // binaries.executable() // Assuming default behavior
        testRuns.maybeCreate("test").enabled = true // Explicitly enable test run
    }

    nodejs {
        testTask { // This is the standard way to configure tests for nodejs
            useMocha {
                timeout = "30s"
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

        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }

        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().forEach { target ->
            val mainSourceSet = sourceSets.getByName("${target.name}Main")
            mainSourceSet.dependsOn(nativeMain)

            val testSourceSet = sourceSets.getByName("${target.name}Test")
            testSourceSet.dependsOn(nativeTest)
        }

        val linuxArm32HfpMain by getting
        val linuxArm32HfpTest by getting
        val androidNativeArm32Main by getting
        val androidNativeArm32Test by getting

        val nodejsMain by getting
        val nodejsTest by getting
    }
}

application {
    mainClass.set("com.example.proxy.MainKt")
}
