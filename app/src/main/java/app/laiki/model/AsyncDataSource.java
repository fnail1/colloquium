package app.laiki.model;

public interface AsyncDataSource<T> {
    int count();

    void requestData();

    T get(int index);

}
