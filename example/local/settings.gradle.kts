pluginManagement {
    repositories {
        flatDir {
            dirs = setOf(file("${rootProject.projectDir}/../../build/libs"))
        }
    }
}
