pluginManagement {
    repositories {
        maven {
            // RetroFuturaGradle
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
            mavenContent {
                includeGroup("com.gtnewhorizons")
                includeGroup("com.gtnewhorizons.retrofuturagradle")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    // Automatic toolchain provisioning
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

dependencyResolutionManagement { 
    versionCatalogs { 
        create("libs") {
            version("kotlinVersion", settings.extra.properties["kotlin_version"].toString())
            version("forgelinContinuousVersion", "2.3.0.0")
        }
    }
}

// Due to an IntelliJ bug, this has to be done
 rootProject.name = "Retro-Sophisticated-Backpacks"
