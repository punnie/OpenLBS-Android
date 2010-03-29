package pt.fraunhofer.openlbs.db;

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
    private static final int DATABASE_VERSION = 2;
    
    /**
     * Redo this crap. It'll be nicer to fetch the statements to
     * the resources insted of writing them here.
     * 
     * Besides that, memory and such.
     */
    
   	private static final String DATABASE_CREATE = 
   		  "create table packages ("
   		+ "_id integer primary key autoincrement,"
   		+ "name varchar(256) not null,"
   		+ "version integer(11));"
  
   		+ "create table locations("
   		+ "id integer primary key autoincrement,"
   		+ "name varchar(256) not null,"
   		+ "coordinates varchar(256),"
   		+ "package_id integer(11) not null);"
  
   		+ "create table contents("
   		+ "id integer primary key autoincrement,"
   		+ "name varchar(256) not null,"
   		+ "path varchar(256) not null,"
   		+ "location_id integer(11) not null);";
   	
	private static final String DATABASE_UPDATE = 
		  "drop table if exists packages;" 
		+ "drop table if exists locations;" 
		+ "drop table if exists contents;";
    
    public static final class Package {
    	public static String TABLE_NAME = "packages";
        public static String ID = "_id";
        public static String NAME = "day";
        public static String VERSION = "version";
        public static String[] COLUMNS = { ID, NAME, VERSION };
    }
    
    public static final class Location {
    	public static String TABLE_NAME = "locations";
        public static String ID = "_id";
        public static String NAME = "name";
        public static String COORDINATES = "coordinates";
        public static String PACKAGE_ID = "package_id";
        public static String[] COLUMNS = { ID, NAME, COORDINATES, PACKAGE_ID };
    }
    
    public static final class Content {
    	public static String TABLE_NAME = "contents";
        public static String ID = "_id";
        public static String NAME = "name";
        public static String PATH = "path";
        public static String LOCATION_ID = "location_id";
        public static String[] COLUMNS = { ID, NAME, PATH, LOCATION_ID };
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
    
    /**
     * Fetches a location by name.
     * @param locationName
     * @return
     */
    
    public Cursor fetchLocation(String locationName){
    	Cursor mCursor = mDb.query(Location.TABLE_NAME, new String[] {Location.NAME, Location.COORDINATES},
    			Location.NAME + "=" + locationName, null, null, null, null);
    	
    	if (mCursor != null) {
            mCursor.moveToFirst();
        }
    	
    	return mCursor;
    }
    
    /**
     * Returns the contents linked to a location.
     * 
     * @param locationName
     * @return mCursor containing all the contents of a location
     */
    
    public Cursor fetchContents(String locationName) {
    	Cursor mCursor = mDb.query(innerJoin(Content.TABLE_NAME, Location.TABLE_NAME, Content.LOCATION_ID, Location.ID), 
    			new String[] {Content.NAME, Content.PATH}, Location.NAME + "=" + locationName, null, null, null, null);
    	return mCursor;
    }
    
    /**
     * Returns an inner join string to feed the query method.
     * 
     * @param table1
     * @param table2
     * @param field1
     * @param field2
     * @return string with the inner join sql syntax
     */
    private String innerJoin(String table1, String table2, String field1, String field2){
    	return table1 + " INNER JOIN " + table2 + " ON " + table1 + "." + field1 + "=" + table2 + "." + field2; 
    }
}
