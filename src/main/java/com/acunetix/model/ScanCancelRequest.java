package com.acunetix.model;

import hudson.util.Secret;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ScanCancelRequest extends ScanRequestBase {
    public ScanCancelRequest(String apiURL, Secret apiToken,String scanTaskId) throws MalformedURLException, NullPointerException, URISyntaxException {
        super(apiURL, apiToken);
        this.scanTaskId = scanTaskId;
        scanCancelUri = new URL(ApiURL, "api/1.0/scans/CancelScanForPlugin/").toURI();
    }

    public final String scanTaskId;
    public final URI scanCancelUri;

    public HttpResponse scanCancelRequest() throws IOException {
        HttpClient client = getHttpClient();
        final HttpPost httpPost = new HttpPost(scanCancelUri + scanTaskId);
        httpPost.setHeader("Accept", json);
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        HttpResponse response = client.execute(httpPost);

        return response;
    }
}
