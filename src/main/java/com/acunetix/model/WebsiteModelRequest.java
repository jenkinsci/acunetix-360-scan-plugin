package com.acunetix.model;

import com.acunetix.utility.AppCommon;
import hudson.util.Secret;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class WebsiteModelRequest extends ScanRequestBase {
	private ArrayList<WebsiteModel> websiteModels = new ArrayList<>();
	private HttpResponse response;

	public WebsiteModelRequest(String apiURL, Secret apiToken)
			throws MalformedURLException, NullPointerException, URISyntaxException {
		super(apiURL, apiToken);
		pluginWebSiteModelsUri = new URL(ApiURL, "api/1.0/scans/PluginWebSiteModels").toURI();
	}

	private final URI pluginWebSiteModelsUri;

	public ArrayList<WebsiteModel> getWebsiteModels() {
		return websiteModels;
	}

	public HttpResponse getPluginWebSiteModels() throws IOException, ParseException {
		final HttpClient httpClient = getHttpClient();
		final HttpGet httpGet = new HttpGet(pluginWebSiteModelsUri);
		httpGet.setHeader("Accept", json);
		httpGet.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

		response = httpClient.execute(httpGet);
		if (response.getStatusLine().getStatusCode() == 200) {
			parseWebsiteData();
		}
		return response;
	}

	private void parseWebsiteData() throws ParseException, IOException {
		String data = AppCommon.parseResponseToString(response);

		JSONParser parser = new JSONParser();
		Object jsonData = parser.parse(data);

		JSONArray WebsiteModelObjects = (JSONArray) jsonData;
		websiteModels = new ArrayList<>();

		for (Object wmo : WebsiteModelObjects) {
			if (wmo instanceof JSONObject) {
				JSONObject websiteModelObject = (JSONObject) wmo;

				WebsiteModel websiteModel = new WebsiteModel();
				websiteModel.setId((String) websiteModelObject.get("Id"));
				websiteModel.setName((String) websiteModelObject.get("Name"));
				websiteModel.setUrl((String) websiteModelObject.get("Url"));

				JSONArray WebsiteProfileModelObjects =
						(JSONArray) websiteModelObject.get("WebsiteProfiles");
				ArrayList<WebsiteProfileModel> profiles = new ArrayList<>();
				for (Object wmpo : WebsiteProfileModelObjects) {
					JSONObject websiteProfileModelObject = (JSONObject) wmpo;

					WebsiteProfileModel websiteProfileModel = new WebsiteProfileModel();
					websiteProfileModel.setId((String) websiteProfileModelObject.get("Id"));
					websiteProfileModel.setName((String) websiteProfileModelObject.get("Name"));

					profiles.add(websiteProfileModel);
				}

				websiteModel.setProfiles(profiles);
				websiteModels.add(websiteModel);
			}
		}
	}
}
