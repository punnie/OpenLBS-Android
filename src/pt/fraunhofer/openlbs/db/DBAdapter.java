package pt.fraunhofer.openlbs.db;

import java.util.ArrayList;

import pt.fraunhofer.openlbs.entities.Package;
import pt.fraunhofer.openlbs.entities.Location;
import pt.fraunhofer.openlbs.entities.Content;

import android.content.Context;
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
    private static final int DATABASE_VERSION = 29;
    
    /**
     * Redo this crap. It'll be nicer to fetch the statements to
     * the resources instead of writing them here.
     * 
     * Besides that, memory and such.
     */
    
   	private static final String DATABASE_CREATE = 
   	  "create table packages ("
   		+ "_id integer primary key,"
   		+ "name varchar(256) not null,"
   		+ "version integer(11) default 0);"

   		+ "create table locations("
   		+ "_id integer primary key,"
   		+ "name varchar(256) not null,"
   		+ "coordinates varchar(256),"
   		+ "tags varchar(256),"
   		+ "package_id integer(11) not null,"
   		+ "foreign key(package_id) references package(_id));"

   		+ "create table contents("
   		+ "_id integer primary key,"
   		+ "name varchar(256) not null,"
   		+ "path varchar(256) not null,"
   		+ "mimetype varchar(32) not null default 'text/plain',"
   		+ "location_id integer(11) not null,"
   		+ "foreign key(location_id) references location(_id));";

   	
	private static final String DATABASE_UPDATE = 
		  "drop table if exists packages;" 
		+ "drop table if exists locations;" 
		+ "drop table if exists contents;";
    
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
    
    public Cursor fetchPackageByName(String packageName){
    	Cursor mCursor = mDb.query(Package.TABLE_NAME, Package.COLUMNS,
    			Package.NAME + "='" + packageName + "'", null, null, null, null);
    	
    	Log.d(TAG, "Result counter: " + mCursor.getCount());
    	
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
    
    public int insertPackage(Package thisPackage) throws SQLException {
    	
    	mDb.execSQL("INSERT INTO packages(_id, name, version) values(?, ?, ?)", 
    			new Object[] {thisPackage.getId(), thisPackage.getName(), thisPackage.getVersion()});
    	
    	Cursor idCursor = mDb.rawQuery("SELECT last_insert_rowid()", new String[] {});
    	
    	if(idCursor.moveToFirst()){
    		Log.d(TAG, "Last insert: " + idCursor.getInt(0));
    		return idCursor.getInt(0);
    	} else {
    		throw new SQLException();
    	}
    }
    
    public int insertLocation(Location thisLocation, int package_id) {
    	Log.d(TAG, "Adding location. Package_id: " + package_id);
    	
    	mDb.execSQL("INSERT INTO locations(name, coordinates, tags, package_id) values(?, ?, ?, ?)", 
				new Object[] { thisLocation.getName(),
						thisLocation.getCoordinates(), thisLocation.getTags(), package_id });
    	
    	Cursor idCursor = mDb.rawQuery("SELECT last_insert_rowid()", new String[] {});
    	
    	if(idCursor.moveToFirst()){
    		Log.d(TAG, "Last insert: " + idCursor.getInt(0));
    		return idCursor.getInt(0);
    	} else {
    		throw new SQLException();
    	}
    }
    
    public int insertContent(Content thisContent, int location_id){
    	Log.d(TAG, "Adding content. Location_id: " + location_id);
    	
    	mDb.execSQL("INSERT INTO contents(name, path, mimetype, location_id) values(?, ?, ?, ?)", 
    			new Object[] { thisContent.getName(), 
    					thisContent.getPath(), thisContent.getMimetype(), location_id });
        	
        	Cursor idCursor = mDb.rawQuery("SELECT last_insert_rowid()", new String[] {});
        	
        	if(idCursor.moveToFirst()){
        		Log.d(TAG, "Last insert: " + idCursor.getInt(0));
        		return idCursor.getInt(0);
        	} else {
        		throw new SQLException();
        	}
    }
    
    /**
     * As there is no ON DELETE CASCADE on this version of sqlite3, lets implement our very own
     * 
     * @param thisPackage
     * @return
     */
    
    public void recursiveDelete(Package thisPackage){
    	ArrayList<Integer> location_ids = new ArrayList<Integer>();
    	ArrayList<Integer> content_ids = new ArrayList<Integer>();
    	
    	Cursor deleteCursor = mDb.rawQuery("SELECT * FROM locations WHERE package_id=?", 
    			new String[] {thisPackage.getId().toString()});
    	
    	while(deleteCursor.moveToNext()){
    		Cursor subDeleteCursor = mDb.rawQuery("SELECT * FROM contents WHERE location_id=?", 
    				new String[] { String.valueOf(deleteCursor.getInt(deleteCursor.getColumnIndex(Location.ID))) });
    		
    		while(subDeleteCursor.moveToNext()){
    			content_ids.add(subDeleteCursor.getInt(subDeleteCursor.getColumnIndex(Content.ID)));
    		}
    		
    		location_ids.add(deleteCursor.getInt(deleteCursor.getColumnIndex(Location.ID)));
    	}
    	
    	for(int i = 0; i < content_ids.size(); i++){
    		mDb.execSQL("DELETE FROM contents WHERE _id=?", new Object[] {content_ids.get(i)});
    		Log.d(TAG, "Deleting content: " + content_ids.get(i));
    	}
    	
    	for(int i = 0; i < location_ids.size(); i++){
    		mDb.execSQL("DELETE FROM locations WHERE _id=?", new Object[] {location_ids.get(i)});
    		Log.d(TAG, "Deleting location: " + location_ids.get(i));
    	}
    	
    	mDb.execSQL("DELETE FROM packages WHERE _id=?", new Object[] {thisPackage.getId()});
    	Log.d(TAG, "Deleting package: " + thisPackage.getId());
    }
    
    /**
     * Method that checks if a given package exists
     * 
     * @param thisPackage
     * @return
     */
    
    public int exists(Package thisPackage){
    	Log.d(TAG, "Checking if package exists: " + thisPackage.getName());
    	
    	Cursor existsCursor = fetchPackageByName(thisPackage.getName());
    	
    	if(existsCursor.moveToNext()){
    		Log.d(TAG, "Package already exists. Updating: " + existsCursor.getInt(existsCursor.getColumnIndex(Package.ID)));
    		return existsCursor.getInt(existsCursor.getColumnIndex(Package.ID));
    	}
    	
    	return 0;
    }
    
    /**
     * Adds a given package to the database
     * 
     * If you find this confusing, email ei04034@gmail.com for a sample of his code, 
     * tell him I sent you
     * 
     * @param thisPackage
     */
    
    public void addPackage(Package thisPackage){
    	int this_package_id = insertPackage(thisPackage);
    	
    	for(int i = 0; i < thisPackage.getLocations().size(); i++){
    		int this_location_id = insertLocation(thisPackage.getLocations().get(i), this_package_id);
    		
    		for(int j = 0; j < thisPackage.getLocations().get(i).getContents().size(); j++){
    			int this_content_id = insertContent(thisPackage.getLocations().get(i).getContents().get(j), this_location_id);
    		}
    	}
    }
    
}
