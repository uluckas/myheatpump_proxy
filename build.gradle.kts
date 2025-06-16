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
    linuxArm32Hfp("linuxArm32Hfp")
    androidNativeArm32("androidNativeArm32") // Added this target

    nodejs {
        testTask {
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

        // Configuring all native targets to share a common nativeMain/nativeTest
        val nativeMain by creating { // ensure this is 'creating' if it might not exist due to no native targets initially
            dependsOn(commonMain)
        }
        val nativeTest by creating { // ensure this is 'creating'
            dependsOn(commonTest)
        }

        // This loop will now apply to linuxArm32Hfp and androidNativeArm32
        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().forEach { target ->
            val mainSourceSet = sourceSets.getByName("${target.name}Main")
            mainSourceSet.dependsOn(nativeMain)

            val testSourceSet = sourceSets.getByName("${target.name}Test")
            testSourceSet.dependsOn(nativeTest)
        }

        val linuxArm32HfpMain by getting
        val linuxArm32HfpTest by getting
        val androidNativeArm32Main by getting // Added
        val androidNativeArm32Test by getting // Added

        val nodejsMain by getting
        val nodejsTest by getting
    }
}

application {
    mainClass.set("com.example.proxy.MainKt")
}
