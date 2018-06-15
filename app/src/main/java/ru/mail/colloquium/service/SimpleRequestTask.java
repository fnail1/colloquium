package ru.mail.colloquium.service;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Response;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.toolkit.http.ServerException;

import static ru.mail.colloquium.App.dateTimeService;

public abstract class SimpleRequestTask<TResponse> extends AbsRequestTask {

    protected SimpleRequestTask(String key) {
        super(key);
    }

    @Override
    protected void performRequest(AppData appData) throws IOException, ServerException {
        Call<TResponse> request = getRequest();
        Response<TResponse> response = request.execute();
        if (response.code() != HttpURLConnection.HTTP_OK)
            throw new ServerException(response);
        TResponse body = response.body();
        if (body == null)
            throw new ServerException(200, key + ": body is null");
        dateTimeService().adjustServerTimeOffset(response);
        processResponse(appData, body);
    }

    protected abstract Call<TResponse> getRequest();

    protected abstract void processResponse(AppData appData, TResponse body);

}
