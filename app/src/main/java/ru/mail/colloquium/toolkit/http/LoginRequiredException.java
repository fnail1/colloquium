package ru.mail.colloquium.toolkit.http;

import java.io.IOException;

public class LoginRequiredException extends IOException {
    public LoginRequiredException(String failedToken) {
        super(new Exception(failedToken));
    }
}
