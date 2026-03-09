package com.project.baedalsodae.global.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.project.baedalsodae.global.common.JsonSerializationException;

public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonUtils() {}

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JsonSerializationException();
        }
    }
}
