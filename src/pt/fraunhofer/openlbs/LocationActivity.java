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
    
	private Uri result;
	
	private int PACKAGE_ID = 0;
	private int LOCATION_ID = 0;
	
	private String PACKAGE_NAME = null;
	private String LOCATION_NAME = null;
	
	private DBAdapter mDBAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);
		
        mDBAdapter = new DBAdapter(getBaseContext());
        mDBAdapter.open();
		
		// TODO: 0. validations
        if(!validateUri(result)){
        	// destroy activity and display a dialog saying it pooped
        }
        	
		result = Uri.parse(getIntent().getExtras().getString(MainActivity.BARCODE_RESULT));
		
		// TODO: 1. extract information from the Uri
		this.PACKAGE_NAME = result.getAuthority();
		this.LOCATION_NAME = result.getPath().split("/")[1];
		
		// TODO: 2. fetch the location info from the database
		populateInfo();
		
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
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * Populates the LocationActivity with the values found on the 
	 * database for that particular location.
	 * 
	 */

	private void populateInfo(){
		if(LOCATION_NAME == null)
			return;
		
		Cursor lcursor = mDBAdapter.fetchLocationByName(PACKAGE_NAME, LOCATION_NAME);
		startManagingCursor(lcursor);
		
		Log.v(TAG, "Location fetched!");
		
		if(lcursor.moveToFirst()){
			TextView locationName = (TextView) findViewById(R.id.locationName);
			TextView packageName = (TextView) findViewById(R.id.packageName);
			TextView locationCoordinates = (TextView) findViewById(R.id.locationCoordinates);
			
			String packageIdentifier = new String();
			String coordinates = new String();
			
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
			
			checkDbLookupIntegrity(lcursor);
			
			LOCATION_ID = lcursor.getInt(lcursor.getColumnIndex(DBAdapter.JoinedLocation.LOCATION_ID));
			PACKAGE_ID = lcursor.getInt(lcursor.getColumnIndex(DBAdapter.JoinedLocation.PACKAGE_ID));
			
			packageIdentifier = lcursor.getString(lcursor.getColumnIndex(DBAdapter.JoinedLocation.PACKAGE_NAME));
			packageIdentifier += ", version ";
			packageIdentifier += lcursor.getString(lcursor.getColumnIndex(DBAdapter.JoinedLocation.VERSION));
			
			packageName.setText(packageIdentifier);
			locationName.setText(lcursor.getString(lcursor.getColumnIndex(DBAdapter.JoinedLocation.LOCATION_NAME)));
			
			coordinates = "coordinates: "; 
			coordinates += lcursor.getString(lcursor.getColumnIndex(DBAdapter.JoinedLocation.COORDINATES));
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
		if (!(LOCATION_NAME.equals(lcursor.getString(lcursor.getColumnIndex(DBAdapter.JoinedLocation.LOCATION_NAME)))))
			Log.e(TAG, "locationName on QRCode not the same as the one referenced in the DB.");
		else
			Log.v(TAG, "locationName on the QRCode seems coincident with the one in the DB.");
		
		if(!(PACKAGE_NAME.equals(lcursor.getString(lcursor.getColumnIndex(DBAdapter.JoinedLocation.PACKAGE_NAME)))))
			Log.e(TAG, "packageName on QRCode not the same as the one referenced in the DB.");
		else
			Log.v(TAG, "packageName on the QRCode seems coincident with the one in the DB.");
	}
	
	/**
	 * Lists all the content a location provides.
	 * 
	 */
	
	private void listContent(){
		if(LOCATION_ID == 0)
			return;
		
		Cursor lcursor = mDBAdapter.fetchContentsByLocationId(LOCATION_ID);
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
					Log.v(TAG, "Content " + id + " (" + ccursor.getString(ccursor.getColumnIndex(DBAdapter.Content.NAME)) 
							+ ", " + ccursor.getString(ccursor.getColumnIndex(DBAdapter.Content.TYPE)) + ") clicked!");
					Log.v(TAG, "Absolute path to data: " + getFilesDir().getAbsolutePath() + "/");
				}
			}
			
		});
	}
}
