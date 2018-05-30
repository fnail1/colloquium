package ru.mail.colloquium.api;

public enum  ApiSet {
    TEST("http://laiki.app/api/"),PROD("https://laiki.app/api/");

    public final String baseUrl;

    ApiSet(String baseUrl) {

        this.baseUrl = baseUrl;
    }
}
