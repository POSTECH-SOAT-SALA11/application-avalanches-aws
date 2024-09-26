package com.avalanches.frameworksanddrivers.databases;

public class JsonMappingCustomException extends RuntimeException {

    public JsonMappingCustomException(String message) {
        super("Erro de mapeamento JSON: " + message);
    }
}
