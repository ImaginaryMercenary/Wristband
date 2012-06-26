package com.nimo.wristband;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper{

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static String DATABASE_NAME;

	// Contacts table name
	private static final String TABLE_BANDS = "bands";
	
	//database path
	private static String DB_PATH = "/data/data/com.nimo.wristband/databases/";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "band_name";
	private static final String KEY_BAND_ID = "band_id";
	private static final String KEY_VENUE = "venue_name";
	private static final String KEY_LATITUDE = "latitude";
	private static final String KEY_LONGITUDE = "longitude";
	private static final String KEY_SONG_TITLE = "song_title";
	private static final String KEY_STREAM = "stream_url";
	private static final String KEY_ART = "art_url";

	public DatabaseHandler(Context context, String dname) {
		super(context, dname, null, DATABASE_VERSION);
		DATABASE_NAME = dname;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_BANDS_TABLE = "CREATE TABLE " + TABLE_BANDS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, "
				+ KEY_BAND_ID + " INTEGER, " 
				+ KEY_VENUE + " TEXT, " + KEY_LATITUDE + " REAL, "
				+ KEY_LONGITUDE + " REAL, " + KEY_SONG_TITLE + " TEXT, "
				+ KEY_STREAM + " TEXT, " + KEY_ART + " TEXT" + ")";
		db.execSQL(CREATE_BANDS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_BANDS);

		// Create tables again
		onCreate(db);
	}

	public void addEntry(String bandName, long bandId, String venueName, double latitude, double longitude, String songTitle, String streamUrl, String artUrl){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(KEY_NAME,bandName);
		values.put(KEY_BAND_ID, bandId);
		values.put(KEY_VENUE, venueName);
		values.put(KEY_LATITUDE, latitude);
		values.put(KEY_LONGITUDE, longitude);
		values.put(KEY_SONG_TITLE, songTitle);
		values.put(KEY_STREAM, streamUrl);
		values.put(KEY_ART, artUrl);

		db.insert(TABLE_BANDS, null, values);
	}
	
	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	public static boolean checkDataBase(String dbName){

	    SQLiteDatabase checkDB = null;

	    try{
	        String myPath = DB_PATH + dbName;
	        checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

	    }catch(SQLiteException e){

	        //database does't exist yet.

	    }

	    if(checkDB != null){

	        checkDB.close();

	    }

	    return checkDB != null ? true : false;
	}

	/**Get all the data stored in the database separated into arrays for each key
	 * 
	 * @param bandList
	 * @param idList
	 * @param venueList
	 * @param coordList
	 * @param titleList
	 * @param streamList
	 * @param artList
	 */
	public void getAllData(String[] bandList, long[] idList, String[] venueList, double[][] coordList, String[] titleList, String[] streamList, String[] artList){
		SQLiteDatabase db = this.getWritableDatabase();

		Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_BANDS, null);

		int i = 0;
		if(cursor.moveToFirst()){
			do{
				bandList[i] = cursor.getString(cursor.getColumnIndex(KEY_NAME));
				idList[i] = cursor.getLong(cursor.getColumnIndex(KEY_BAND_ID));
				venueList[i] = cursor.getString(cursor.getColumnIndex(KEY_VENUE));
				coordList[i][0] = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
				coordList[i][1] = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
				titleList[i] = cursor.getString(cursor.getColumnIndex(KEY_SONG_TITLE));
				streamList[i] = cursor.getString(cursor.getColumnIndex(KEY_STREAM));
				artList[i] = cursor.getString(cursor.getColumnIndex(KEY_ART));
				++i;
			}while(cursor.moveToNext());
		}

	}
	
	public int getNumberEntries(){
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_BANDS, null);
		
		return cursor.getCount();
	}
	
	public String getDBName(){
		return DATABASE_NAME;
	}
}
