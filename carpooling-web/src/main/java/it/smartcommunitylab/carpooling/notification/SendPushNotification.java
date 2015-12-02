package it.smartcommunitylab.carpooling.notification;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SendPushNotification {

	@Autowired
	private Environment env;

	private String channel = "CarPooling_";

	private Map<String, String> parseheaders = new HashMap<String, String>();

	@PostConstruct
	void init() {
		parseheaders.put("X-Parse-Application-Id", env.getProperty("parse.application.key"));
		parseheaders.put("X-Parse-REST-API-Key", env.getProperty("parse.rest.api.key"));

	}

	public String sendNotification(String providerKey, String userId, String msgType, String msg)
			throws MalformedURLException, JSONException {

		String result = "failed";
		URL url = new URL(env.getProperty(providerKey));

		if (providerKey.equalsIgnoreCase("parse.api.uri")) {
			/**
			 *
			{
				"channels": ["CarPooling_53"],
				"data": {
					"alert": "I am waiting at Povo Manci."
				}
			}
			 */

			JSONArray channelIdArray = new JSONArray().put(channel + userId);

			JSONObject channels = new JSONObject();
			channels.put("$in", channelIdArray);

			JSONObject whereClause = new JSONObject();
			whereClause.put("channels", channels);

			JSONObject dataClause = new JSONObject();
			dataClause.put(msgType, msg);

			JSONObject request = new JSONObject();
			request.put("data", dataClause);
			request.put("where", whereClause);

			JSONObject response = SendServerRequest.sendJSONRequest(url, parseheaders, request.toString(), true);

			if (response != null && response.has("result") && response.getBoolean("result")) {
				result = "success";
			}

		}

		return result;

	}

}

class SendServerRequest {
	static JSONObject sendJSONRequest(URL url, Map<String, String> headers, String request, boolean secure) {

		try {

			if (secure) {

				HttpsURLConnection connection = null;

				connection = (HttpsURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setDoInput(true);
				connection.setDoOutput(true);

				// additional headers(if any).
				if (headers != null && !headers.isEmpty()) {
					for (String header : headers.keySet()) {
						connection.setRequestProperty(header, headers.get(header));
					}
				}

				DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
				writer.write(request.getBytes("UTF-8"));
				writer.flush();
				writer.close();

				return parseResponse(connection);

			} else {

				HttpURLConnection connection = null;

				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setDoInput(true);
				connection.setDoOutput(true);

				// additional headers(if any).
				if (headers != null && !headers.isEmpty()) {
					for (String header : headers.keySet()) {
						connection.setRequestProperty(header, headers.get(header));
					}
				}

				DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
				writer.write(request.getBytes("UTF-8"));
				writer.flush();
				writer.close();

				return parseResponse(connection);

			}

		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
			return null;
		}

	}

	static JSONObject parseResponse(HttpURLConnection connection) throws IOException, JSONException {
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder response = new StringBuilder();

		while ((line = reader.readLine()) != null) {
			response.append(line).append('\r');
		}
		reader.close();

		return new JSONObject(response.toString());
	}
}
