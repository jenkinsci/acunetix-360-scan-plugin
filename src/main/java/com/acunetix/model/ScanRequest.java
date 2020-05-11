package com.acunetix.model;

import hudson.util.Secret;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ScanRequest extends ScanRequestBase {
    public ScanRequest(String apiURL, Secret apiToken, String scanType, String websiteId,
            String profileId, VCSCommit vcsCommit)
            throws MalformedURLException, NullPointerException, URISyntaxException {
        super(apiURL, apiToken);
        this.scanType = ScanType.valueOf(scanType);
        this.websiteId = websiteId;
        this.profileId = profileId;
        this.vcsCommit = vcsCommit;
        scanUri = new URL(ApiURL, "api/1.0/scans/CreateFromPluginScanRequest").toURI();
        testUri = new URL(ApiURL, "api/1.0/scans/VerifyPluginScanRequest").toURI();
    }

    public final ScanType scanType;
    public final String websiteId;
    public final String profileId;
    public final VCSCommit vcsCommit;
    public final URI scanUri;
    public final URI testUri;

    public HttpResponse scanRequest() throws IOException {
        HttpClient client = getHttpClient();
        final HttpPost httpPost = new HttpPost(scanUri);
        httpPost.setHeader("Accept", json);
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        List<NameValuePair> params = new ArrayList<>();
        setScanParams(params);
        vcsCommit.addVcsCommitInfo(params);
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse response = client.execute(httpPost);

        return response;
    }

    public HttpResponse testRequest() throws IOException {
        HttpClient client = getHttpClient();
        final HttpPost httpPost = new HttpPost(testUri);
        httpPost.setHeader("Accept", json);
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        List<NameValuePair> params = new ArrayList<>();
        setScanParams(params);
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse response = client.execute(httpPost);

        return response;
    }

    private void setScanParams(List<NameValuePair> params) {
        switch (scanType) {
            case Incremental:
                params.add(new BasicNameValuePair("WebsiteId", websiteId));
                params.add(new BasicNameValuePair("ProfileId", profileId));
                params.add(new BasicNameValuePair("ScanType", "Incremental"));
                break;
            case FullWithPrimaryProfile:
                params.add(new BasicNameValuePair("WebsiteId", websiteId));
                params.add(new BasicNameValuePair("ScanType", "FullWithPrimaryProfile"));
                break;
            case FullWithSelectedProfile:
                params.add(new BasicNameValuePair("WebsiteId", websiteId));
                params.add(new BasicNameValuePair("ProfileId", profileId));
                params.add(new BasicNameValuePair("ScanType", "FullWithSelectedProfile"));
                break;
        }
    }
}
