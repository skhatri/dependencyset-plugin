pluginManagement {
    repositories {
        flatDir {
            dirs = setOf(File("${rootProject.projectDir}/../build/libs"))
        }
    }
}