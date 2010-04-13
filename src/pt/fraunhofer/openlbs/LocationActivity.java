package pt.fraunhofer.openlbs;

import pt.fraunhofer.openlbs.db.DBAdapter;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class LocationActivity extends Activity {
    private static final String TAG = "OpenLBS LocationActivity";
    
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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);
		
        mDBAdapter = new DBAdapter(getBaseContext());
        mDBAdapter.open();
        
        myLocation = new Location();
        myPackage = new Package();
		
		// TODO: 0. validations
        if(!validateUri(result)){
        	// destroy activity and display a dialog saying it pooped
        }
		
		try {
			populateFields();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	/**
	 * Validates a provided URI before sending it to be parsed.
	 * 
	 * @param result
	 * @return
	 */
	
	private boolean validateUri(Uri result) {
		// TODO: Actually validate the URI
		return true;
	}
	
	/**
	 * Populates both the Location and Package fields.
	 * 
	 * @throws Exception
	 */
	
	private void populateFields() throws Exception {
		result = Uri.parse(getIntent().getExtras().getString(MainActivity.BARCODE_RESULT));
		
		myPackage.name = result.getAuthority();
		myLocation.name = result.getPath().split("/")[1];
		
		if(myLocation.name == null || myPackage.name == null)
			throw new Exception();
		
		Cursor lcursor = mDBAdapter.fetchLocationByName(myPackage.name, myLocation.name);
		startManagingCursor(lcursor);
		
		Log.v(TAG, "Location fetched!");
		
		/* 
		 * Debug log garbage below.
		 */
		
		Log.v(TAG, "package_id key index: " + lcursor.getColumnIndex(DBAdapter.JoinedLocation.PACKAGE_ID));
		Log.v(TAG, "package_name key index: " + lcursor.getColumnIndex(DBAdapter.JoinedLocation.PACKAGE_NAME));
		Log.v(TAG, "location_id key index: " + lcursor.getColumnIndex(DBAdapter.JoinedLocation.LOCATION_ID));
		Log.v(TAG, "location_name key index: " + lcursor.getColumnIndex(DBAdapter.JoinedLocation.LOCATION_NAME));
		Log.v(TAG, "coordinates key index: " + lcursor.getColumnIndex(DBAdapter.JoinedLocation.COORDINATES));
		Log.v(TAG, "version key index: " + lcursor.getColumnIndex(DBAdapter.JoinedLocation.VERSION));
		
		/*
		 * End of debug log garbage.
		 */
		
		if(lcursor.moveToFirst()){
			checkDbLookupIntegrity(lcursor);
			
			myLocation._id = lcursor.getInt(lcursor.getColumnIndex(DBAdapter.JoinedLocation.LOCATION_ID));
			myLocation.coordinates = lcursor.getString(lcursor.getColumnIndex(DBAdapter.JoinedLocation.COORDINATES));
			
			myPackage._id = lcursor.getInt(lcursor.getColumnIndex(DBAdapter.JoinedLocation.PACKAGE_ID));
			myPackage.version = lcursor.getInt(lcursor.getColumnIndex(DBAdapter.JoinedLocation.VERSION));
			
			// TODO: check if fields are correctly filled.
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
	
		coordinates = "coordinates: " + myLocation.coordinates;
		locationCoordinates.setText(coordinates);
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
		
		String[] from = new String[] {DBAdapter.Content.NAME, DBAdapter.Content.PATH};
		int[] to = new int[] {R.id.contentName, R.id.contentPath};
		
		SimpleCursorAdapter contents = new SimpleCursorAdapter(this, R.layout.contents_row, lcursor, from, to);
		
		ListView contentList = (ListView) findViewById(R.id.locationContents);
		contentList.setAdapter(contents);
		
		contentList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				// TODO Auto-generated method stub
				Cursor ccursor = mDBAdapter.fetchContentById(id);
				startManagingCursor(ccursor);
				
				if(ccursor.moveToFirst() && (ccursor.getCount() == 1)){
					/*
					Log.v(TAG, "Content " + id + ", position " + position + " (" + ccursor.getString(ccursor.getColumnIndex(DBAdapter.Content.NAME)) 
							+ ", " + ccursor.getString(ccursor.getColumnIndex(DBAdapter.Content.TYPE)) + ") clicked!");
					Log.v(TAG, "Absolute path to data: " + getFilesDir().getAbsolutePath() + "/" + myPackage.name + "/" + myLocation.name + "/" + ccursor.getString(ccursor.getColumnIndex(DBAdapter.Content.PATH)));
					*/
					
					
					
				}
			}
			
		});
	}
}
