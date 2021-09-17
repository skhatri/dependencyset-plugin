plugins {
  id("com.github.skhatri.dependencyset") version "0.1.0"
}

appConfig {
  main.set("com.plugins.Application")
  lang.value(listOf("java", "kotlin"))
  implementationItems.value(listOf("spring-boot", "jackson", "coroutines", "kotlin"))
  testImplementationItems.value(listOf("junit"))
}
