package com.acunetix.utility;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.base.Charsets;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class AppCommon {
	public static List<String> getNames(Class<? extends Enum<?>> e) {
		String[] enumNames =
				Arrays.toString(e.getEnumConstants()).replaceAll("^.|.$", "").split(", ");
		return Arrays.asList(enumNames);
	}

	public static boolean isUrlValid(String url) {
		String[] schemes = {"http", "https"}; // DEFAULT schemes = "http", "https", "ftp"
		UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);

		if (urlValidator.isValid(url)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isValidEmailAddress(String email) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}

	public static boolean isGUIDValid(String guid) {
		try {
			if (guid == null) {
				return false;
			}
			UUID.fromString(
					// fixes the guid if it doesn't contain hypens
					guid.replaceFirst(
							"(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
							"$1-$2-$3-$4-$5"));
			return true;
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}

	public static URL getBaseURL(String url) throws MalformedURLException {
		return new URL(new URL(url), "/");
	}

	public static String mapToQueryString(Map<String, String> map) {
		StringBuilder stringBuilder = new StringBuilder();
		String key;
		String value;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append("&");
			}
			try {
				key = entry.getKey();
				value = entry.getValue();
				stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
				stringBuilder.append("=");
				stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("This method requires UTF-8 encoding support", e);
			}
		}

		return stringBuilder.toString();
	}

	public static Map<String, String> queryStringToMap(String input) {
		Map<String, String> map = new HashMap<>();

		String[] nameValuePairs = input.split("&");
		for (String nameValuePair : nameValuePairs) {
			String[] nameValue = nameValuePair.split("=");
			try {
				map.put(URLDecoder.decode(nameValue[0], "UTF-8"),
						nameValue.length > 1 ? URLDecoder.decode(nameValue[1], "UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("This method requires UTF-8 encoding support", e);
			}
		}

		return map;
	}

	public static Object parseJsonValue(String Data, String key) throws ParseException {
		JSONParser parser = new JSONParser();
		Object parsedData = parser.parse(Data);
		Object value;
		if (parsedData instanceof JSONArray) {
			JSONArray array = (JSONArray) parsedData;
			JSONObject object = (JSONObject) array.get(0);
			value = object.get(key);
		} else {
			JSONObject obj = (JSONObject) parsedData;
			value = obj.get(key);
		}
		return value;
	}

	public static String parseResponseToString(HttpResponse response) throws IOException {
		return IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
	}

	public static StandardUsernamePasswordCredentials findCredentialsById(
			final String credentialsId, final String descriptorUrlOrJobUrl)
			throws UnsupportedEncodingException {
		final Jenkins jenkins = Jenkins.get();

		/*
		 * This case is without folder plugin "job/Project Name"
		 */
		ItemGroup credentialsContext = (ItemGroup) jenkins;

		List<Folder> folders = jenkins.getItems(Folder.class);

		/*
		 * if folders are used then find project's context first
		 */
		if (folders != null && folders.size() > 0) {

			List<String> folderNames = getFolderNames(descriptorUrlOrJobUrl);

			folderNames.removeIf(item -> item == null || "".equals(item));

			// last one is project name so remove it
			folderNames.remove(folderNames.size() - 1);

			Folder deepestFolder = getDeepestFolder(folders, folderNames);

			if (folders.size() > 0 && deepestFolder != null
					&& CredentialsProvider.hasStores(deepestFolder)) {
				credentialsContext = (ItemGroup) deepestFolder;
			}
		}

		List<StandardUsernamePasswordCredentials> matches =
				CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class,
						credentialsContext, ACL.SYSTEM, (DomainRequirement) null);

		final CredentialsMatcher matcher = CredentialsMatchers.withId(credentialsId);
		final StandardUsernamePasswordCredentials result =
				CredentialsMatchers.firstOrNull(matches, matcher);

		return result;
	}

	private static List<String> getFolderNames(final String descriptorUrlOrJobUrl)
			throws UnsupportedEncodingException {
		List<String> folderNames;
		String projectFullUrl = descriptorUrlOrJobUrl;

		// if its descriptorUrl
		if (descriptorUrlOrJobUrl.contains("/job/")) {
			/*
			 * Format is like:
			 * 
			 * "job/Folder Name/job/Project Name" or
			 * "job/Folder Name/job/FolderInsideFolder/job/Project Name"
			 */
			projectFullUrl =
					URLDecoder.decode(descriptorUrlOrJobUrl, StandardCharsets.UTF_8.toString());

			// substring url to make it starts with "job"
			if (!projectFullUrl.startsWith("/job")) {
				projectFullUrl = projectFullUrl.substring(projectFullUrl.indexOf("/job"));
			}

			projectFullUrl = projectFullUrl.replace("/job/", "/");

		}

		// then it's a job url similar to this
		// "Folder 1/Folder 1 - Folder 1/Folder 1 - Folder 1 - Folder 1/Child Project"

		folderNames = new java.util.ArrayList<>(java.util.Arrays.asList(projectFullUrl.split("/")));

		return folderNames;
	}

	private static Folder getDeepestFolder(List<Folder> folders, List<String> folderNames) {
		if (folders.size() == 0 || folderNames.size() == 0) {
			return null;
		}
		Folder deepestFolder = folders.get(0);
		Folder tempFolder;

		Optional<Folder> f =
				folders.stream().filter(w -> w.getName().equals(folderNames.get(0))).findFirst();
		if (f.isPresent()) {
			tempFolder = f.get();
			if (CredentialsProvider.hasStores(tempFolder)) {
				deepestFolder = tempFolder;
			}

			for (int i = 1; i < folderNames.size(); i++) {
				tempFolder = (Folder) tempFolder.getItem(folderNames.get(i));
				if (tempFolder != null && CredentialsProvider.hasStores(tempFolder)) {
					deepestFolder = tempFolder;
				}
			}
		}

		return deepestFolder;
	}

	public static List<StandardUsernamePasswordCredentials> findCredentials(Item own) {

		final List<StandardUsernamePasswordCredentials> matches =
				CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class,
						own, ACL.SYSTEM, (DomainRequirement) null);
		return matches;
	}
}
