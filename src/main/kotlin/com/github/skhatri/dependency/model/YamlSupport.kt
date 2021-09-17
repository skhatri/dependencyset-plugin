package com.github.skhatri.dependency.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File


class YamlSupport {
    private val yamlMapper = YAMLMapper()
    private val factory = YAMLFactory()

    init {
        yamlMapper.registerModule(KotlinModule())
    }

    fun toMap(fileName: String): Map<*, *> {
        return yamlMapper.readValue(File(fileName), Map::class.java)
    }

    fun toList(fileName: String): List<ObjectNode> {
        val parser = factory.createParser(File(fileName))
        val typeRef = object : TypeReference<ObjectNode>() {}
        return yamlMapper.readValues(parser, typeRef).readAll()
    }

    fun <T> toInstance(fileName: String, clz: Class<T>): T {
        return yamlMapper.readValue(File(fileName), clz)
    }

    fun <T> toInstance(data: ByteArray, clz: Class<T>): T {
        return yamlMapper.readValue(data, clz)
    }
}
