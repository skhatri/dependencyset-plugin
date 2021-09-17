package com.github.skhatri.dependency.model


data class DependencySet(val id: String, val items: List<String>)
data class DependencyConfig(
    val dependencySet: List<DependencySet>?,
)
