package ru.mail.colloquium.model;

public interface AsyncDataSource<T> {
    int count();

    void requestData();

    T get(int index);

}
