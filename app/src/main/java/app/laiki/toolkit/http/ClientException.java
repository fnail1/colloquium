package app.laiki.toolkit.http;

import java.io.IOException;

public class ClientException extends Exception {
    public ClientException(IOException e) {
        super(e);
    }

    public ClientException() {

    }
}
