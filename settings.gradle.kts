pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven ("https://maven.fabricmc.net/" )
    }
}

rootProject.name = "BlueCommands"

include("bluecommands-core")
include("bluecommands-brigadier")
//include("fabric-test-mod")
