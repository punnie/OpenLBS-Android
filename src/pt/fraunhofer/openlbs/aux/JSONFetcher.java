package pt.fraunhofer.openlbs.aux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONFetcher {
	public static JSONArray getHttpJsonArray(String url) throws JSONException, ClientProtocolException, IOException {
		JSONArray json = null;
		String result = getHttp(url);
		json = new JSONArray(result);
		
		return json;
	}
	
	public static JSONObject getHttpJson(String url) throws JSONException, ClientProtocolException, IOException {
		JSONObject json = null;
		String result = getHttp(url);
		json = new JSONObject(result);
		
		return json;
	}
	
	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private static String getHttp(String url) throws ClientProtocolException, IOException {
		String result = "";
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;

		response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream instream = entity.getContent();
			result = convertStreamToString(instream);
			// Do something about this
			instream.close();
		}
		return result;
	}
}
