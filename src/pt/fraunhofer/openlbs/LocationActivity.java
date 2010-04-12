package pt.fraunhofer.openlbs;

import pt.fraunhofer.openlbs.db.DBAdapter;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

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
	
	private boolean validateUri(Uri result) {
		// TODO Auto-generated method stub
		return false;
	}

	private void populateInfo(){
		if(LOCATION_NAME == null)
			return;
		
		Cursor lcursor = mDBAdapter.fetchLocationByName(LOCATION_NAME);
		startManagingCursor(lcursor);
		
		Log.v(TAG, "Location fetched!");
		
		if(lcursor.moveToFirst()){
			TextView locationName = (TextView) findViewById(R.id.locationName);
			TextView packageName = (TextView) findViewById(R.id.packageName);
			TextView locationCoordinates = (TextView) findViewById(R.id.locationCoordinates);
			
			Log.v(TAG, "_id key index: " + lcursor.getColumnIndex(DBAdapter.Location.ID));
			Log.v(TAG, "name key index: " + lcursor.getColumnIndex(DBAdapter.Location.NAME));
			Log.v(TAG, "coordinates key index: " + lcursor.getColumnIndex(DBAdapter.Location.COORDINATES));
			Log.v(TAG, "package_id key index: " + lcursor.getColumnIndex(DBAdapter.Location.PACKAGE_ID));
			
			LOCATION_ID = lcursor.getInt(lcursor.getColumnIndex(DBAdapter.Location.ID));
			PACKAGE_ID = lcursor.getInt(lcursor.getColumnIndex(DBAdapter.Location.PACKAGE_ID));
			
			if(PACKAGE_ID > 0){
				Cursor pcursor = mDBAdapter.fetchPackageById(PACKAGE_ID);
				startManagingCursor(pcursor);
				
				if(pcursor.moveToFirst()){
					String packageIdentifier = pcursor.getString(pcursor.getColumnIndex(DBAdapter.Package.NAME));
					packageIdentifier += ", version ";
					packageIdentifier += pcursor.getString(pcursor.getColumnIndex(DBAdapter.Package.VERSION));
					
					packageName.setText(packageIdentifier);
				}
			}
			
			locationName.setText(lcursor.getString(lcursor.getColumnIndex(DBAdapter.Location.NAME)));
			
			String coordinates = "coordinates: "; 
			coordinates += lcursor.getString(lcursor.getColumnIndex(DBAdapter.Location.COORDINATES));
			locationCoordinates.setText(coordinates);
		}
	}
	
	private void listContent(){
		if(LOCATION_ID == 0)
			return;
		
		Cursor lcursor = mDBAdapter.fetchContentsById(LOCATION_ID);
		String[] from = new String[] {DBAdapter.Content.NAME, DBAdapter.Content.PATH};
		int[] to = new int[] {R.id.contentName, R.id.contentPath};
		
		SimpleCursorAdapter contents = new SimpleCursorAdapter(this, R.layout.contents_row, lcursor, from, to);
		
		ListView contentList = (ListView) findViewById(R.id.locationContents);
		contentList.setAdapter(contents);
	}
}
