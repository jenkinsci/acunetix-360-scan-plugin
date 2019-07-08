package com.acunetix.utility;

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
import java.util.*;


public class AppCommon{
	public static List<String> getNames(Class<? extends Enum<?>> e) {
		String[] enumNames = Arrays.toString(e.getEnumConstants()).replaceAll("^.|.$", "").split(", ");
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
					//fixes the guid if it doesn't contain hypens
					guid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
					));
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
				key=entry.getKey();
				value=entry.getValue();
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
				map.put(URLDecoder.decode(nameValue[0], "UTF-8"), nameValue.length > 1 ? URLDecoder.decode(
						nameValue[1], "UTF-8") : "");
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
		return IOUtils.toString(response.getEntity().getContent());
	}
}