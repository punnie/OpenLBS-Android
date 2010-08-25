package pt.fraunhofer.openlbs;

import java.io.IOException;
import java.util.Vector;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.fraunhofer.openlbs.aux.Constants;
import pt.fraunhofer.openlbs.aux.INETTools;
import pt.fraunhofer.openlbs.aux.JSONFetcher;
import pt.fraunhofer.openlbs.db.DBAdapter;
import pt.fraunhofer.openlbs.entities.Content;
import pt.fraunhofer.openlbs.entities.Location;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ShowPackageActivity extends Activity {
	private static final String TAG = "ShowPackagesActivity";
	private static final String URL = Constants.OPENLBS_BASE_URL + "/packages/#.json";

	private static final int THREAD_SUCCESS = 0;
	private static final int THREAD_BAD_JSON = 1;
	private static final int THREAD_BAD_INTERNET = 2;

	private static final int DIALOG_NO_INTERNET = 0;
	private static final int DIALOG_BAD_JSON = 1;
	private static final int DIALOG_BAD_INTERNET = 2;
	private static final int DIALOG_PACKAGE_ID = 3;
	
	private static final int REQUEST_INSTALL = 0;

	private Package thisPackage;
	private Vector<Location> locations;
	private String thisPackageURL;
	private ProgressDialog progressDialog;
	private LocationListItemAdapter locationListAdapter;

	private Runnable fetchList = new Runnable() {

		public void run() {
			JSONObject jsonPackage = null;
			Message msg = handler.obtainMessage();
			Bundle b = new Bundle();
			
			try{
				jsonPackage = JSONFetcher.getHttpJson(thisPackageURL);
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
				jsonPackage = jsonPackage.getJSONObject("package");
				thisPackage.setName(jsonPackage.getString("name"));
				thisPackage.setUpdated_at(jsonPackage.getString("updated_at"));
				thisPackage.setVersion(jsonPackage.getInt("version"));
				thisPackage.setContent_file_name(jsonPackage.getString("content_file_name"));
				JSONArray jsonLocations = jsonPackage.getJSONArray("locations");
				
				for(int i = 0; i < jsonLocations.length(); i++){
					Location l = new Location();
					l.setName(((JSONObject)jsonLocations.get(i)).getString("name"));
					l.setTags(((JSONObject)jsonLocations.get(i)).getString("tags"));
					l.setCoordinates(((JSONObject)jsonLocations.get(i)).getString("coordinates"));
					
					JSONArray jsonContents = ((JSONObject)jsonLocations.get(i)).getJSONArray("contents");
					
					for(int j = 0; j < jsonContents.length(); j++){
						Content c = new Content();
						c.setName(((JSONObject)jsonContents.get(j)).getString("name"));
						c.setPath(((JSONObject)jsonContents.get(j)).getString("path"));
						c.setTags(((JSONObject)jsonContents.get(j)).getString("tags"));
						c.setMimetype(((JSONObject)jsonContents.get(j)).getString("mimetype"));
						
						l.addContent(c);
					}
					
					locations.add(l);
				}

				thisPackage.setLocations(locations);

				/*
				
				DBAdapter mDb = new DBAdapter(getBaseContext());
				mDb.open();

				Button installButton = (Button) findViewById(R.id.InstallButton);

				if (mDb.exists(thisPackage) > 0) {
					installButton.setVisibility(View.GONE);
				}

				mDb.close();
				
				*/
				
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
				fillPackageInformation();
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
	
	private void fillPackageInformation() {
		Log.d(TAG, "fillPackageInformation thisPackage.name: " + thisPackage.getName());
		TextView textPackageName = (TextView) findViewById(R.id.thisPackageName);
		TextView textPackageContentFileName = (TextView) findViewById(R.id.thisPackageContentFileName);
		TextView textPackageUpdatedAt = (TextView) findViewById(R.id.thisPackageUpdatedAt);
		
		if(textPackageName != null){
			textPackageName.setText(thisPackage.getName());
		}
		
		if(textPackageContentFileName != null){
			textPackageContentFileName.setText(thisPackage.getContent_file_name());
		}
		
		if(textPackageUpdatedAt != null){
			textPackageUpdatedAt.setText(String.valueOf(thisPackage.getUpdated_at()));
		}
		
		locationListAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.packageshow);
		
		thisPackage = new Package();
		locations = new Vector<Location>();
		thisPackage.setId(getIntent().getExtras().getInt(ListPackagesActivity.PACKAGE_ID));
		thisPackageURL = URL.replace("#", thisPackage.getId().toString());
		
		locationListAdapter = new LocationListItemAdapter(this,
				R.layout.packages_row, locations);
		ListView locationList = (ListView) findViewById(R.id.thisPackageLocations);
		locationList.setAdapter(locationListAdapter);
		
		if (INETTools.hasInternet(this)) {
			Thread thread = new Thread(null, fetchList, "JSONFetch");
			progressDialog = ProgressDialog.show(this, "Fetching package data",
					"Package information being fetched. Hold on a jiff...");
			thread.start();
			
			Button installButton = (Button) findViewById(R.id.InstallButton);
			installButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					Intent i = new Intent(getApplicationContext(), InstallPackageActivity.class);
					Bundle b = new Bundle();
					b.putSerializable("package", thisPackage);
					i.putExtras(b);
					
					thisPackage = (Package) b.getSerializable("package");
					
					startActivityForResult(i, REQUEST_INSTALL);
				}
			});
			
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
	    case DIALOG_PACKAGE_ID:
	    	builder = new AlertDialog.Builder(this);
	    	builder.setMessage("PackageID received: " + thisPackageURL)
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
