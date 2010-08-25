package pt.fraunhofer.openlbs;

import java.io.File;

import pt.fraunhofer.openlbs.aux.Constants;
import pt.fraunhofer.openlbs.db.DBAdapter;
import pt.fraunhofer.openlbs.entities.Content;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ShowLocationActivity extends Activity {
    private static final String TAG = "OpenLBS LocationActivity";
    
    private static final int DIALOG_INVALID_QRCODE = 0;
    
	private class Location {
		public int _id;
		public String coordinates;
		public String name;
	}
	
	private class Package {
		public int _id;
		public int version;
		public String name;
	}
    
	private Uri result;
	
	private DBAdapter mDBAdapter;
	
	private Location myLocation;
	private Package myPackage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "Entered onCreate() state!");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.locationshow);
		
        mDBAdapter = new DBAdapter(getBaseContext());
        mDBAdapter.open();
        
        myLocation = new Location();
        myPackage = new Package();
        
		/* 
		 * Use the Uri class to parse the barcode scan result.
		 * 
         * If the result isn't an uri, this will return nothing, making result
         * a null object. Hence, upon validation, we must predict a 
         * NullPointerException happening. 
         * 
         */
        
        if(result == null){
        	Log.d(TAG, "Result is null!");
        	result = Uri.parse(getIntent().getExtras().getString(MainActivity.BARCODE_RESULT));
        } else {
        	Log.d(TAG, "Result is NOT null!");
        }
		
		try {
			if (!validateUri(result)) {
				showDialog(DIALOG_INVALID_QRCODE);
			}
		} catch (NullPointerException e) {
			showDialog(DIALOG_INVALID_QRCODE);
		}
		
		try {
			populateFields();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			finish();
		}
		
		// TODO: 2. fetch the location info from the database
		try {
			populateInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO: 3. list the location contents
		listContent();
		
	}
	
	@Override
	protected void onRestart() {
		Log.d(TAG, "Entered onRestart() state!");
		super.onRestart();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "Entered onSaveInstanceState() state!");
		outState.putString("result", result.toString());		
		
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "Entered onRestoreInstanceState() state!");
		result = Uri.parse(savedInstanceState.getString("result"));
		
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "Entered onPause() state!");
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.d(TAG, "Entered onStop() state!");
		
		//mDBAdapter.close();
		super.onStop();
	}

	/**
	 * Validates a provided URI before sending it to be parsed.
	 * 
	 * @param result
	 * @return
	 * @throws Exception 
	 */
	
	private boolean validateUri(Uri result) {
	
		return true;
	}
	
	/**
	 * Populates both the Location and Package fields.
	 * 
	 * @throws Exception
	 */
	
	private void populateFields() throws Exception {
		myPackage.name = result.getAuthority();
		myLocation.name = result.getPath().split("/")[1];
		
		if(myLocation.name == null || myPackage.name == null)
			throw new Exception();
		
		Cursor lcursor = mDBAdapter.fetchLocationByName(myPackage.name, myLocation.name);
		startManagingCursor(lcursor);
		
		if(lcursor.moveToFirst()){
			checkDbLookupIntegrity(lcursor);
			
			myLocation._id = lcursor.getInt(lcursor.getColumnIndex(DBAdapter.JoinedLocation.LOCATION_ID));
			myLocation.coordinates = lcursor.getString(lcursor.getColumnIndex(DBAdapter.JoinedLocation.COORDINATES));
			
			myPackage._id = lcursor.getInt(lcursor.getColumnIndex(DBAdapter.JoinedLocation.PACKAGE_ID));
			myPackage.version = lcursor.getInt(lcursor.getColumnIndex(DBAdapter.JoinedLocation.VERSION));
			
			// TODO: check if fields are correctly filled.
		} else {
			throw new Exception();
		}
	}
	
	/**
	 * Populates the LocationActivity with the values found on the 
	 * database for that particular location.
	 * @throws Exception  
	 * 
	 */

	private void populateInfo() throws Exception {
		
		if(myPackage == null || myLocation == null)
			throw new Exception();
		
		TextView locationName = (TextView) findViewById(R.id.locationName);
		TextView packageName = (TextView) findViewById(R.id.packageName);
		TextView locationCoordinates = (TextView) findViewById(R.id.locationCoordinates);
		
		String packageIdentifier = new String();
		String coordinates = new String();
		
		packageIdentifier = myPackage.name + ", version " + myPackage.version;
		
		packageName.setText(packageIdentifier);
		locationName.setText(myLocation.name);
	
		if(myLocation.coordinates != null){
			coordinates = "coordinates: " + myLocation.coordinates;
			locationCoordinates.setText(coordinates);
		}
	}
	
	/**
	 * Checks the integrity of the values found in the QRCode against
	 * the values found on the DB.
	 * 
	 * TODO: actually throw some exceptions in case something goes bad.
	 * 
	 * @param lcursor
	 */
	
	private void checkDbLookupIntegrity(Cursor lcursor){
		if (!(myLocation.name.equals(lcursor.getString(lcursor.getColumnIndex(DBAdapter.JoinedLocation.LOCATION_NAME)))))
			Log.e(TAG, "locationName on QRCode not the same as the one referenced in the DB.");
		else
			Log.v(TAG, "locationName on the QRCode seems coincident with the one in the DB.");
		
		if(!(myPackage.name.equals(lcursor.getString(lcursor.getColumnIndex(DBAdapter.JoinedLocation.PACKAGE_NAME)))))
			Log.e(TAG, "packageName on QRCode not the same as the one referenced in the DB.");
		else
			Log.v(TAG, "packageName on the QRCode seems coincident with the one in the DB.");
	}
	
	/**
	 * Lists all the content a location provides.
	 * 
	 */
	
	private void listContent(){
		if(myLocation._id == 0)
			return;
		
		Cursor lcursor = mDBAdapter.fetchContentsByLocationId(myLocation._id);
		startManagingCursor(lcursor);
		
		String[] from = new String[] {Content.NAME, Content.PATH};
		int[] to = new int[] {R.id.contentName, R.id.contentPath};
		
		SimpleCursorAdapter contents = new SimpleCursorAdapter(this, R.layout.contents_row, lcursor, from, to);
		
		ListView contentList = (ListView) findViewById(R.id.locationContents);
		contentList.setAdapter(contents);
		
		contentList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				Cursor ccursor = mDBAdapter.fetchContentById(id);
				startManagingCursor(ccursor);
				
				if(ccursor.moveToFirst() && (ccursor.getCount() == 1)){
					String filePath = Constants.APP_CONTENT_DATA_FOLDER + "/" 
					+ myPackage.name + "/" + myLocation.name + "/" 
					+ ccursor.getString(ccursor.getColumnIndex(Content.PATH));
					
					String fileType = ccursor.getString(ccursor.getColumnIndex(Content.TYPE));
					
					Log.v(TAG, "Content " + id + ", position " + position 
							+ " (" + ccursor.getString(ccursor.getColumnIndex(Content.NAME)) 
							+ ", " + ccursor.getString(ccursor.getColumnIndex(Content.TYPE)) 
							+ ") clicked!");
					Log.v(TAG, "Absolute path to data: " + filePath);
					
					Intent intent = new Intent();  
					intent.setAction(android.content.Intent.ACTION_VIEW);
					File file = new File(filePath);
					boolean result = file.mkdirs();
					
					Log.v(TAG, "File URI: " + Uri.fromFile(file));
					
					intent.setDataAndType(Uri.fromFile(file), fileType);
					startActivity(intent);
				}
			}
			
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
	    switch(id) {
	    case DIALOG_INVALID_QRCODE:
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("Invalid QRCode read. You may want to try again.")
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
