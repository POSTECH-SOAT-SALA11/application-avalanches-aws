package com.avalanches.interfaceadapters.presenters;

import com.avalanches.frameworksanddrivers.databases.JsonMappingCustomException;
import com.avalanches.frameworksanddrivers.databases.JsonProcessingCustomException;
import com.avalanches.interfaceadapters.presenters.interfaces.JsonPresenterInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonPresenter implements JsonPresenterInterface {

    private final ObjectMapper objectMapper;

    public JsonPresenter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public <T> String serialize(T object) throws JsonProcessingCustomException, JsonMappingCustomException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonMappingException ex) {
            throw new JsonMappingCustomException(ex.getMessage());
        } catch (JsonProcessingException ex) {
            throw new JsonProcessingCustomException(ex.getMessage());
        }
    }

    @Override
    public <T> T deserialize(String serializedObject, Class<T> targetClass) throws JsonProcessingCustomException, JsonMappingCustomException {
        try {
            return objectMapper.readValue(serializedObject, targetClass);
        } catch (JsonMappingException ex) {
            throw new JsonMappingCustomException(ex.getMessage());
        } catch (JsonProcessingException ex) {
            throw new JsonProcessingCustomException(ex.getMessage());
        }
    }
}
