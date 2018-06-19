package app.laiki.toolkit.http;

import java.io.IOException;

import retrofit2.Response;

public class ServerException extends Exception {
    private final int code;
    private String errorBody;

    public ServerException(int code) {
        super("" + code);
        this.code = code;
    }

    public ServerException(int code, String message) {
        super("" + code + ' ' + message);
        this.code = code;
    }

    public <T> ServerException(Response<T> response) {
        this(response.code(), response.message());
        try {
            errorBody = response.errorBody().string();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }


    public int getCode() {
        return code;
    }

    public String getErrorBody() {
        return errorBody;
    }
}
