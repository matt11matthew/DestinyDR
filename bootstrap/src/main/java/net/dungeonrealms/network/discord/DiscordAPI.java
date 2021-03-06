package net.dungeonrealms.network.discord;

import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;

/**
 * Discord API - Perform actions on the DR discord server.
 * @author Kneesnap
 */
public class DiscordAPI {
	
	/**
	 * Sends a message to the specified discord channel.
	 * @param channel
	 * @param message
	 */
	public static void sendMessage(DiscordChannel channel, String message) {
		JsonObject postData = channel.getPostData();
		postData.addProperty("content", stripColor(message));
		sendRequest(channel.getURL(), postData.toString());
	}
	
	private static String stripColor(String message) {
		Pattern stripColor = Pattern.compile("(?i)" + String.valueOf('\247') + "[0-9A-FK-OR]");
		return stripColor.matcher(message).replaceAll("");
	}
	
	private static void sendRequest(String url, String postData) {
		new Thread(() -> {
			try {
        		HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
        		con.setReadTimeout(5000);
        		con.setRequestMethod("POST");
        		con.setRequestProperty("Content-Type", "application/json");
        		con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11");
        		con.setDoOutput(true);
        		con.setDoInput(true);
        		OutputStream out = con.getOutputStream();
        		out.write(postData.getBytes());
        		out.flush();
        		out.close();
        		
        		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        		StringBuilder result = new StringBuilder();
        		String line;
        		while((line = reader.readLine()) != null)
        		    result.append(line);
        	} catch (Exception e) {
            	e.printStackTrace();
        	}
		}).start();
	}
}
