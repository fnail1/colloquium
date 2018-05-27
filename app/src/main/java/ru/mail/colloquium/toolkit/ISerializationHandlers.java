package ru.mail.colloquium.toolkit;

public interface ISerializationHandlers {
    void onBeforeSerialization();
    void onAfterDeserialization();
}
