pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven { url = uri("https://developer.huawei.com/repo/") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/repositories/releases/") }
    }
    versionCatalogs {
        create("androidx") {
            from(files("gradle/androidx.versions.toml"))
        }
        create("kotlinx") {
            from(files("gradle/kotlinx.versions.toml"))
        }
    }
}

rootProject.name="DanDanPlayForAndroid"

include(":app")
include(":local_component")
include(":anime_component")
include(":user_component")
include(":storage_component")
include(":player_component")
include(":common_component")
include(":data_component")

include(":repository:danmaku")
include(":repository:immersion_bar")
include(":repository:mmkv:annotation")
include(":repository:mmkv:processor")
include(":repository:panel_switch")
include(":repository:seven_zip")
include(":repository:thunder")
include(":repository:video_cache")