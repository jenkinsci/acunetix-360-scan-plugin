package com.acunetix.model;

import java.util.ArrayList;

public class WebsiteModel {
	private String id;
	private String name;
	private String url;
	private ArrayList<WebsiteProfileModel> profiles;

	public WebsiteModel() {
		profiles = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDisplayName() {
		return getName() + " (" + getUrl() + ")";
	}

	public ArrayList<WebsiteProfileModel> getProfiles() {
		return profiles;
	}

	public void setProfiles(ArrayList<WebsiteProfileModel> profiles) {
		this.profiles = profiles;
	}
}
