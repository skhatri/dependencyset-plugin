initscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.github.skhatri:gradle-reposet-plugin:0.1.2")
    }
}

apply<com.github.skhatri.gradle.init.ReposetPlugin>()


