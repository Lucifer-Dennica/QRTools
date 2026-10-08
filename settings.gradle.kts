pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // RuStore SDK
        maven {
            url = uri("https://nexus-external.rustore.ru/repository/maven-rustore-exposed")
        }
    }
}

rootProject.name = "QRTools"
include(":app")
