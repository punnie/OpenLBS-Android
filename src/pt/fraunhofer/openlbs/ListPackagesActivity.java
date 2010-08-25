package pt.fraunhofer.openlbs;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import pt.fraunhofer.openlbs.aux.Constants;
import pt.fraunhofer.openlbs.aux.INETTools;
import pt.fraunhofer.openlbs.aux.JSONFetcher;
import pt.fraunhofer.openlbs.entities.Package;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
	private static final String URL = Constants.OPENLBS_BASE_URL + "/packages.json";
	
	private static final int THREAD_SUCCESS = 0;
	private static final int THREAD_BAD_JSON = 1;
	private static final int THREAD_BAD_INTERNET = 2;
	
	private static final int DIALOG_NO_INTERNET = 0;
	private static final int DIALOG_BAD_JSON = 1;
	private static final int DIALOG_BAD_INTERNET = 2;

	private ArrayList<Package> packages;
	private ProgressDialog progressDialog;
	private PackageListItemAdapter packageListAdapter;
	
	public static final String PACKAGE_ID = "PackageId";

	private Runnable fetchList = new Runnable() {

		public void run() {
			JSONArray jsonPackages = null;
			Message msg = handler.obtainMessage();
			Bundle b = new Bundle();
			
			try{
				jsonPackages = JSONFetcher.getHttpJsonArray(URL);
			} catch (JSONException e) {
				b.putInt("status", THREAD_BAD_JSON);
				e.printStackTrace();
				msg.setData(b);
				handler.sendMessage(msg);
			} catch (ClientProtocolException e) {
				b.putInt("status", THREAD_BAD_JSON);
				e.printStackTrace();
				msg.setData(b);
				handler.sendMessage(msg);
			} catch (IOException e) {
				b.putInt("status", THREAD_BAD_JSON);
				e.printStackTrace();
				msg.setData(b);
				handler.sendMessage(msg);
			} 
			
			try {
				for (int i = 0; i < jsonPackages.length(); i++) {
					Package newPackage = new Package();
					newPackage.setId(jsonPackages.getJSONObject(i)
							.getJSONObject("package").getInt("id"));
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

			b.putInt("status", THREAD_SUCCESS);
			msg.setData(b);
			handler.sendMessage(msg);
		}
	};

	final Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			int status = msg.getData().getInt("status");
			switch (status) {
			case THREAD_SUCCESS:
				packageListAdapter.notifyDataSetChanged();
				progressDialog.dismiss();
				break;
			case THREAD_BAD_JSON:
				showDialog(DIALOG_BAD_JSON);
				break;
			case THREAD_BAD_INTERNET:
				showDialog(DIALOG_BAD_INTERNET);
				break;
			default:
				return;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.packagelist);
		
		if (INETTools.hasInternet(this)) {

			packages = new ArrayList<Package>();

			packageListAdapter = new PackageListItemAdapter(this,
					R.layout.packages_row, packages);
			ListView packageList = (ListView) findViewById(R.id.availablePackages);
			packageList.setAdapter(packageListAdapter);

			packageList.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					
					Bundle extras = new Bundle();
					extras.putInt(PACKAGE_ID, packages.get(arg2).getId());
					
					Intent intent = new Intent(getApplicationContext(), ShowPackageActivity.class);
					intent.putExtras(extras);
					
					startActivity(intent);
				}
			});

			Thread thread = new Thread(null, fetchList, "JSONFetch");
			progressDialog = ProgressDialog.show(this, "Fetching data",
					"Available packages being fetched. Hold on a jiff...");
			thread.start();
		} else {
			showDialog(DIALOG_NO_INTERNET);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
	    switch(id) {
	    case DIALOG_NO_INTERNET:
	    	builder = new AlertDialog.Builder(this);
	    	builder.setMessage("No connection to the Internet!")
	    	       .setCancelable(false)
	    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                finish();
	    	           }
	    	       });
	    	dialog = builder.create();
	        break;
	    case DIALOG_BAD_JSON:
	    	builder = new AlertDialog.Builder(this);
	    	builder.setMessage("Malformed JSON received (transmission error?).")
	    	       .setCancelable(false)
	    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                finish();
	    	           }
	    	       });
	    	dialog = builder.create();
	        break;
	    case DIALOG_BAD_INTERNET:
	    	builder = new AlertDialog.Builder(this);
	    	builder.setMessage("There was a transmission error. You may want to try again later.")
	    	       .setCancelable(false)
	    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                finish();
	    	           }
	    	       });
	    	dialog = builder.create();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}

}
