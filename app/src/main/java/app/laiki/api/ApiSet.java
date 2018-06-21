package app.laiki.api;

public enum  ApiSet {
    TEST("http://laiki.app", "http://testpoint.laiki.app/api/", true),
    PROD("https://laiki.app", "https://laiki.app/api/", false);

    public final String webUrl;
    public final String baseUrl;
    public final boolean fixSsl;

    ApiSet(String webUrl, String baseUrl, boolean fixSsl) {
        this.webUrl = webUrl;
        this.baseUrl = baseUrl;
        this.fixSsl = fixSsl;
    }

    public String fixSslForSandbox(String src) {
        if (fixSsl && src != null && src.startsWith("https://"))
            return "http://" + src.substring(8);

        return src;
    }
}
