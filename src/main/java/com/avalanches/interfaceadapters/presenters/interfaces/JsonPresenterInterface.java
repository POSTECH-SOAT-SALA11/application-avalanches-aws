package com.avalanches.interfaceadapters.presenters.interfaces;

public interface JsonPresenterInterface {

    <T> String serialize(T object);

    <T> T deserialize(String serializedObject, Class<T> targetClass);

}
