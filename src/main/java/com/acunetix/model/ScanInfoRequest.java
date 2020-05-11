package com.acunetix.model;

import hudson.util.Secret;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ScanInfoRequest extends ScanRequestBase {
    public ScanInfoRequest(String apiURL, Secret apiToken,String scanTaskId) throws MalformedURLException, NullPointerException, URISyntaxException {
        super(apiURL, apiToken);
        this.scanTaskId = scanTaskId;
        scanInfoUri = new URL(ApiURL, "api/1.0/scans/ScanInfoForPlugin/").toURI();
    }

    public final String scanTaskId;
    public final URI scanInfoUri;

    public HttpResponse scanInfoRequest() throws IOException {
        HttpClient client = getHttpClient();
        final HttpGet httpGet = new HttpGet(scanInfoUri + scanTaskId);
        httpGet.setHeader("Accept", json);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        HttpResponse response = client.execute(httpGet);

        return response;
    }
}
