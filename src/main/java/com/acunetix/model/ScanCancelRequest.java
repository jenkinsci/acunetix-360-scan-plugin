package com.acunetix.model;

import hudson.util.Secret;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ContentType;

public class ScanCancelRequest extends ScanRequestBase {
    public ScanCancelRequest(String apiURL, Secret apiToken, String scanTaskId,
     ProxyBlock proxy) throws MalformedURLException, NullPointerException, URISyntaxException {
        super(apiURL, apiToken, proxy);
        this.scanTaskId = scanTaskId;
        this.scanCancelUri = new URL(ApiURL, "api/1.0/scans/CancelScanForPlugin/").toURI();
        this.contentLength = "0";
        this.content = "";
    }

    public final String scanTaskId;
    public final URI scanCancelUri;
    public final String contentLength;
    public final String content;

    public ClassicHttpResponse scanCancelRequest() throws IOException {
        CloseableHttpClient client = getHttpClient();
        HttpPost httpPost = new HttpPost(scanCancelUri + scanTaskId);
        HttpEntity stringEntity = new StringEntity(content,ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        httpPost.setHeader("Accept", json);
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        return (ClassicHttpResponse) client.execute(httpPost);
    }
}
