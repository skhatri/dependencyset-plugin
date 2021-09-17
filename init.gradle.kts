initscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.github.skhatri.reposet:com.github.skhatri.reposet.gradle.plugin:0.1.2")
    }
}
apply<com.github.skhatri.gradle.init.ReposetPlugin>()
