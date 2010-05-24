package pt.fraunhofer.openlbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.fraunhofer.openlbs.entities.Package;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ListPackagesActivity extends Activity {
	private static final String TAG = "ListPackagesActivity";
	private static final String URL = "http://ni.fe.up.pt/~pedro/packages.json";
	
	private static final int THREAD_SUCCESS = 0;

	private ArrayList<Package> packages;
	private ProgressDialog progressDialog;
	private PackageListItemAdapter packageListAdapter;

	private Runnable fetchList = new Runnable() {

		public void run() {
			JSONArray jsonPackages = getHttpJsonArray(URL);

			try {
				for (int i = 0; i < jsonPackages.length(); i++) {
					Package newPackage = new Package();
					newPackage.setId(i);
					newPackage.setName(jsonPackages.getJSONObject(i)
							.getJSONObject("package").getString("name"));
					newPackage.setVersion(jsonPackages.getJSONObject(i)
							.getJSONObject("package").getInt("version"));
					newPackage.setUpdated_at(jsonPackages.getJSONObject(i)
							.getJSONObject("package").getString("updated_at"));

					packages.add(newPackage);
				}
			} catch (JSONException e) {
				// do something about this
			}

			Message msg = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putInt("status", THREAD_SUCCESS);
			msg.setData(b);
			handler.sendMessage(msg);
		}
	};

	final Handler handler = new Handler() {
		
		public void handleMessage(Message msg) {
			int status = msg.getData().getInt("status");
			if (status == THREAD_SUCCESS) {
				packageListAdapter.notifyDataSetChanged();
				progressDialog.dismiss();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.packagelist);

		packages = new ArrayList<Package>();

		packageListAdapter = new PackageListItemAdapter(this,
				R.layout.packages_row, packages);
		ListView packageList = (ListView) findViewById(R.id.availablePackages);
		packageList.setAdapter(packageListAdapter);
		
		packageList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Log.v(TAG, "Clicked on item: " + packages.get(arg2).getName());
			}
		});

		Thread thread = new Thread(null, fetchList, "MagentoBackground");
		progressDialog = ProgressDialog.show(this, "Fetching data",
				"Available packages being fetched. Hold on a jiff.");
		thread.start();
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

	public JSONArray getHttpJsonArray(String url) {
		JSONArray json = null;
		String result = getHttp(url);
		try {
			json = new JSONArray(result);
		} catch (JSONException e) {
			Log.e(TAG, "There was a Json parsing based error", e);
		}
		return json;
	}
	
	// Will be needed for later use.
	public JSONObject getHttpJson(String url) {
		JSONObject json = null;
		String result = getHttp(url);
		try {
			json = new JSONObject(result);
		} catch (JSONException e) {
			Log.e(TAG, "There was a Json parsing based error", e);
		}
		return json;
	}

	public String getHttp(String url) {
		Log.d(TAG, "getHttp : " + url);
		String result = "";
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				result = convertStreamToString(instream);
				Log.i(TAG, result);
				instream.close();
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "There was a protocol based error", e);
		} catch (IOException e) {
			Log.e(TAG, "There was an IO Stream related error", e);
		}
		return result;
	}

}
