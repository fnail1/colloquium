package app.laiki.api;

public enum  ApiSet {
    TEST("http://laiki.app/api/", true),
    PROD("https://laiki.app/api/", false);

    public final String baseUrl;
    public final boolean fixSsl;

    ApiSet(String baseUrl, boolean fixSsl) {
        this.baseUrl = baseUrl;
        this.fixSsl = fixSsl;
    }

    public String fixSslForSandbox(String src) {
        if (fixSsl && src != null && src.startsWith("https://"))
            return "http://" + src.substring(8);

        return src;
    }
}
