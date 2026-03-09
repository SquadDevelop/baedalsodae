package com.project.baedalsodae.global.common;

public class JsonSerializationException extends BusinessException {

    public JsonSerializationException() {
        super(ErrorCode.JSON_SERIALIZATION_ERROR);
    }
}
