package it.smartcommunitylab.carpooling.notification;

import it.smartcommunitylab.carpooling.model.Notification;
import it.smartcommunitylab.carpooling.utils.CarPoolingUtils;

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
	private URL apiURL = null;

	@PostConstruct
	void init() throws MalformedURLException {
		parseheaders.put("X-Parse-Application-Id", env.getProperty("parse.application.key"));
		parseheaders.put("X-Parse-REST-API-Key", env.getProperty("parse.rest.api.key"));
		apiURL = new URL(env.getProperty("parse.api.uri"));
	}

	public boolean sendNotification(String userId, Notification n)
			throws JSONException {

		boolean result = false;

		JSONArray channelIdArray = new JSONArray().put(channel + userId);

		JSONObject channels = new JSONObject();
		channels.put("$in", channelIdArray);

		JSONObject whereClause = new JSONObject();
		whereClause.put("channels", channels);

		JSONObject dataClause = new JSONObject();
		whereClause.put("channels", channels);
		dataClause.put("cp_type", n.getType());
		dataClause.put("cp_travelId", n.getTravelId());

		String title = constructTitle(n);
		dataClause.put("alert", title);

		dataClause.put("urlHash", "#/app/notifiche");
		dataClause.put("url", "#/app/notifiche");

		if (n.getData() != null) {
			for (String key : n.getData().keySet()) {
				dataClause.put("cp_"+key, n.getData().get(key));
			}
		}

		JSONObject request = new JSONObject();
		request.put("data", dataClause);
		request.put("where", whereClause);

		JSONObject response = SendServerRequest.sendJSONRequest(apiURL, parseheaders, request.toString(), true);

		if (response != null && response.has("result") && response.getBoolean("result")) {
			result = true;
		}

		return result;

	}

	/**
	 * @param n
	 * @return
	 */
	private String constructTitle(Notification n) {
		if (CarPoolingUtils.NOTIFICATION_CONFIRM.equals(n.getType())) {
			return  n.getData().get("status").equals("1") ?  "Viaggio confermato": "Viaggio rifiutato";
		}
		if (CarPoolingUtils.NOTIFICATION_CHAT.equals(n.getType())) {
			return  "Nuovo messaggio da "+ n.getData().get("senderFullName");
		}
		if (CarPoolingUtils.NOTIFICATION_AVALIABILITY.equals(n.getType())) {
			return  "Trovato un viaggio";
		}
		if (CarPoolingUtils.NOTIFICATION_BOOKING.equals(n.getType())) {
			return n.getData().get("senderFullName") + " chiede di partecipare al tuo viaggio";
		}
		return null;
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
