package com.acunetix.model;

import com.acunetix.utility.AppCommon;
import hudson.util.Secret;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class ScanRequestBase {
    protected static final String json = "application/json";
    public final URL ApiURL;
    public final Secret ApiToken;

    // Called from server-side
    public ScanRequestBase(String apiURL, Secret apiToken) throws MalformedURLException {
        this.ApiURL = AppCommon.getBaseURL(apiURL);
        this.ApiToken = apiToken;
    }

    public ScanRequestBase() {
        this.ApiURL = null;
        this.ApiToken = null;
    }

    protected CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().disableRedirectHandling().build();
    }

    protected String getAuthHeader() {
        String auth = ":" + ApiToken.getPlainText();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.ISO_8859_1);

        return authHeader;
    }
}
