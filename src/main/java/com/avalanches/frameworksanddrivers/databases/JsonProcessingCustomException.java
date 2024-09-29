package com.avalanches.frameworksanddrivers.databases;

public class JsonProcessingCustomException extends RuntimeException {
    public JsonProcessingCustomException(String message) {
        super("Erro ao processar JSON: " + message);
    }
}
