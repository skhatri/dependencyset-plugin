### Intro
This plugin helps manage dependencies for your project through a shared dependencyset.


### Why this?
Once you have a bunch of microservices, updating library versions can be a pain. Sure, you might have a template and it is maintained. Your project soon loses connection with the template and you might have tons of dependencies which are not uptodate. Each team is different and you want to update these dependencies for your project at your own pace.

This plugin is an effort towards making that happen.

### How do I use it?

Create a build.gradle.kts like so:

```gradle

plugins {
    id("com.github.skhatri.dependencyset") version "0.1.0"
}

appConfig {
    main.set("com.plugins.Application")
    lang.value(listOf("java", "kotlin"))
    implementationItems.value(listOf("spring-boot", "jackson", "coroutines", "kotlin"))
    testImplementationItems.value(listOf("junit"))
}
```

or using legacy mode


```gradle
buildscript {
  dependencies {
    classpath("com.github.skhatri:com.github.skhatri.dependencyset.gradle.plugin:0.1.0")
  }
}

apply<com.github.skhatri.dependency.DependencySetPlugin>()
appConfig {
    main.set("com.plugins.Application")
    lang.value(listOf("java", "kotlin"))
    implementationItems.value(listOf("spring-boot", "jackson", "coroutines", "kotlin"))
    testImplementationItems.value(listOf("junit"))
}
```


these few lines of gradle config will configure your application to use kotlin and springboot. It will also pull in required test dependencies.

### Can I add my own dependencies?

Absolutely, that is the idea. We call put dependencies in small bundles and we call them dependencyset. Dependencyset can be local or remote. More on this soon.



