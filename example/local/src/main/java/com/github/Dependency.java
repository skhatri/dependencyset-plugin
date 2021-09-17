package com.github;

import com.fasterxml.jackson.core.JsonGenerator;

public class Dependency {

    public static void main(String[] args) {
        Class<JsonGenerator> clz1 = JsonGenerator.class;
        Class<JsonGenerator> clz2 = JsonGenerator.class;
        assert clz1 == clz2;
    }
}