package pt.fraunhofer.openlbs.db;

import java.io.IOException;
import java.io.InputStream;
import pt.fraunhofer.openlbs.R;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
    private static final String TAG = "OpenLBS DBAdapter";
    private static DatabaseHelper mDbHelper;
    private static SQLiteDatabase mDb;
    private final Context mCtx;
    
    /**
     * Database creation sql statements
     */

    private static final String DATABASE_NAME = "openlbs.sqlite3";
    private static final int DATABASE_VERSION = 8;
    
    /**
     * Redo this crap. It'll be nicer to fetch the statements to
     * the resources instead of writing them here.
     * 
     * Besides that, memory and such.
     */
    
   	private static final String DATABASE_CREATE = 
   		  "create table packages ("
   		+ "_id integer primary key autoincrement,"
   		+ "name varchar(256) not null,"
   		+ "version integer(11));"
  
   		+ "create table locations("
   		+ "_id integer primary key autoincrement,"
   		+ "name varchar(256) not null,"
   		+ "coordinates varchar(256),"
   		+ "tags varchar(256),"
   		+ "package_id integer(11) not null);"
  
   		+ "create table contents("
   		+ "_id integer primary key autoincrement,"
   		+ "name varchar(256) not null,"
   		+ "path varchar(256) not null,"
   		+ "mimetype varchar(32) not null default 'text/plain',"
   		+ "location_id integer(11) not null);";
   	
	private static final String DATABASE_UPDATE = 
		  "drop table if exists packages;" 
		+ "drop table if exists locations;" 
		+ "drop table if exists contents;";
    
    public static final class Package {
    	public static String TABLE_NAME = "packages";
        public static String ID = "_id";
        public static String NAME = "name";
        public static String VERSION = "version";
        public static String[] COLUMNS = { ID, NAME, VERSION };
    }
    
    public static final class Location {
    	public static String TABLE_NAME = "locations";
        public static String ID = "_id";
        public static String NAME = "name";
        public static String COORDINATES = "coordinates";
        public static String TAGS = "tags";
        public static String PACKAGE_ID = "package_id";
        public static String[] COLUMNS = { ID, NAME, COORDINATES, TAGS, PACKAGE_ID };
    }
    
    public static final class Content {
    	public static String TABLE_NAME = "contents";
        public static String ID = "_id";
        public static String NAME = "name";
        public static String PATH = "path";
        public static String TYPE = "mimetype";
        public static String LOCATION_ID = "location_id";
        public static String[] COLUMNS = { ID, NAME, PATH, TYPE, LOCATION_ID };
    }
    
    public static final class JoinedLocation {
    	
    	// No TABLE_NAME, as this abstracts a joined table.
    	
    	public static String PACKAGE_ID = "package_id";
    	public static String PACKAGE_NAME = "package_name";
    	public static String LOCATION_ID = "location_id";
    	public static String LOCATION_NAME = "location_name";
    	public static String VERSION = "version";
    	public static String COORDINATES = "coordinates";
    	public static String TAGS = "tags";
    	public static String[] COLUMNS = { PACKAGE_ID, PACKAGE_NAME, LOCATION_ID, LOCATION_NAME, VERSION, COORDINATES, TAGS };
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	for(String createStatements: DATABASE_CREATE.split(";"))
        		db.execSQL(createStatements);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            
            for(String updateStatements: DATABASE_UPDATE.split(";"))
        		db.execSQL(updateStatements);
            
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        
        // TODO: remove when updating from remote
        if(!databasePopulated())
        	populateDummyData();
        
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    public Cursor fetchPackageById(int packageId){
    	Cursor mCursor = mDb.query(Package.TABLE_NAME, Package.COLUMNS,
    			Package.ID + "='" + packageId + "'", null, null, null, null);
    	
    	return mCursor;
    }
    
    /**
     * Fetches a location by name.
     * @param locationName
     * @return
     */
    
    public Cursor fetchLocationByName(String packageName, String locationName){
    	
    	Cursor mCursor = mDb.rawQuery("SELECT packages._id as package_id, "
    			+ "packages.name as package_name, packages.version, "
    			+ "locations._id as location_id, locations.name as location_name, "
    			+ "locations.coordinates, locations.tags "
    			+ "FROM locations INNER JOIN packages ON locations.package_id=packages._id "
    			+ "WHERE locations.name=? AND packages.name=?", 
    			new String[] {locationName, packageName});
    	
    	Log.v(TAG, "mCursor row count: " + mCursor.getCount());
    	
    	return mCursor;
    }
    
    /**
     * Returns the contents linked to a location.
     * 
     * @param locationId
     * @return mCursor containing all the contents of a location
     */
    
    public Cursor fetchContentsByLocationId(int locationId) {
    	Cursor mCursor = mDb.query(Content.TABLE_NAME, Content.COLUMNS, 
    			Content.LOCATION_ID + "='" + locationId + "'", null, null, null, null);
    	
    	return mCursor;
    }
    
    /**
     * Returns a content selected by its id
     * 
     * @param contentId
     * @return
     */
    
    public Cursor fetchContentById(long contentId) {
    	Cursor mCursor = mDb.query(Content.TABLE_NAME, Content.COLUMNS, 
    			Content.ID + "='" + contentId + "'", null, null, null, null);
    	
    	return mCursor;
    }
    
    /**
     * Down right fugly testing dummy data injection
     */
    
    // TODO: remove when updating from remote
    
    private String getFileContent(Resources resources, int rawId) throws IOException
      {
        InputStream is = resources.openRawResource(rawId);
        int size = is.available();
        // Read the entire asset into a local byte buffer.
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        // Convert the buffer into a string.
        return new String(buffer);
      }
    
    private void populateDummyData(){
    	String[] fixtures = null;
    	
		try {
			fixtures = new String [] {getFileContent(this.mCtx.getResources(), R.raw.db_contents_fixtures),
					getFileContent(this.mCtx.getResources(), R.raw.db_locations_fixtures),
					getFileContent(this.mCtx.getResources(), R.raw.db_packages_fixtures)};
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(fixtures == null)
			return;
    	
    	for(int i = 0; i < fixtures.length; i++){
    		for(String statement: fixtures[i].split(";"))
    			mDb.execSQL(statement);
    	}
    }
    
    private boolean databasePopulated(){
    	Cursor mCursor = mDb.query(Location.TABLE_NAME, 
    			new String[] {Location.NAME, Location.COORDINATES},
    			null, null, null, null, null);
    	
    	return mCursor.getCount() > 0;
    }
}
