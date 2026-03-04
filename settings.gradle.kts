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
    }
}

rootProject.name = "NativeTutorAI"
include(":app")
include(":language-engine-private")

// Link to the private engine directory relative to this project
project(":language-engine-private").projectDir = file("../language-engine-private")
