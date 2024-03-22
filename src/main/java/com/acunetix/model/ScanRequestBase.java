package com.acunetix.model;

import com.acunetix.utility.AppCommon;
import hudson.util.Secret;
import org.apache.commons.codec.binary.Base64;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class ScanRequestBase {
    protected static final String json = "application/json";
    public final URL ApiURL;
    public final Secret ApiToken;
    public final ProxyBlock proxy; 

    // Called from server-side
    public ScanRequestBase(String apiURL, Secret apiToken, ProxyBlock proxy) throws MalformedURLException {
        this.ApiURL = AppCommon.getBaseURL(apiURL);
        this.ApiToken = apiToken;
        this.proxy = proxy;
    }

    public ScanRequestBase() {
        this.ApiURL = null;
        this.ApiToken = null;
        this.proxy = null;
    }

    protected CloseableHttpClient getHttpClient() {
  
        if (proxy != null && proxy.getUseProxy()) {
            int proxyPort = Integer.parseInt(proxy.getpPort());
            HttpHost proxyHttpHost = new HttpHost(proxy.getpHost(), proxyPort);
            
            BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
            AuthScope authScope = new AuthScope(proxyHttpHost);
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                proxy.getpUser(), proxy.getpPassword().toCharArray());
            credsProvider.setCredentials(authScope, credentials);

            return HttpClientBuilder
                    .create()
                    .setProxy(proxyHttpHost)
                    .setDefaultCredentialsProvider(credsProvider)
                    .disableRedirectHandling()
                    .build();
        }
        else {
            return HttpClientBuilder
                    .create()
                    .disableRedirectHandling()
                    .build();
        }   
    }

    protected String getAuthHeader() {
        String auth = ":" + ApiToken.getPlainText();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.ISO_8859_1);

        return authHeader;
    }
}
