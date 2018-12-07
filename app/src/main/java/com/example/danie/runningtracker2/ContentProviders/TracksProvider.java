package com.example.danie.runningtracker2.ContentProviders;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

public class TracksProvider extends ContentProvider {
    private static final String TAG = "TracksProvider";
    public static final String PROVIDER_NAME = "com.example.danie.runningtracker2.ContentProviders.TracksProvider";
    public static final String PATH = "tracks";
    public static final String URL = "content://"+PROVIDER_NAME;
    public static final Uri CONTENT_URI = Uri.parse(URL);
    public static final String ANDROID_CURSOR_DIR = "vnd.android.cursor.dir";

    //fields
    public static final String ID = "id";
    public static final String START_LOCATION = "startLocation";
    public static final String END_LOCATION = "endLocation";
    public static final String DISTANCE = "distance";
    public static final String DURATION = "duration";
    public static final String UNIT = "unit";

    private static final int uriCode = 1;
    private static HashMap<String, String> values;

    private static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, PATH, uriCode);
    }

    //db
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private static final String DB_NAME = "tracksDB";
    private static final String TABLE_NAME = "tracksTable";
    private static final int DB_VERSION = 1;

    //sql
    private static final String CREATE_TABLE =
            "CREATE TABLE "+ TABLE_NAME +
                    "(" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    START_LOCATION + " TEXT NOT NULL, " +
                    END_LOCATION + " TEXT, " +
                    DISTANCE + " TEXT, " +
                    DURATION + " TEXT, "+
                    UNIT + "TEXT"+
                    ");";

    private static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public TracksProvider() {
    }


    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        db = dbHelper.getWritableDatabase();

        //if db doesn't exists already, create one
        return db != null;
    }

    /*
     * uri = unique uri of the content provider
     * projection = an array of fields intended to retrieve with query
     * selection = WHERE XXX IS YYY
     * selectionArgs = an array of YYY, intended to be used with selection
     * */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query: ");
        Cursor cursor=null;

        try{

            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(TABLE_NAME);

            switch(uriMatcher.match(uri)){
                case uriCode:
                    qb.setProjectionMap(values);    //all fields intended to retrieve
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: "+uri);
            }

            //default sortOrder is by ID if none specified
            if(sortOrder.isEmpty()){
                sortOrder = ID;
            }

            cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);  //set onchange listener

        }catch(Exception e){
            Log.d(TAG, "query: "+e);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        switch(uriMatcher.match(uri)){
            case uriCode:
                return ANDROID_CURSOR_DIR+"/"+PATH;
            default:
                throw new IllegalArgumentException("Unsupported URI: "+uri);
        }
    }

    @Override
    public Uri insert(Uri uri,  ContentValues values) {
        long rowID = db.insert(TABLE_NAME, null, values);

        //if new record inserted successfully
        if(rowID>0){
            Log.d(TAG, "insert: Success");
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);

            return _uri;
        }else{
            Log.d(TAG, "insert: Failed");
        }

        throw new SQLException("Failed to add a record into " + uri);
    }


    @Override
    public int delete(Uri uri,  String selection,  String[] selectionArgs) {
        int rowsDeleted = 0;

        switch(uriMatcher.match(uri)){
            case uriCode:
                rowsDeleted = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: "+uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri,  ContentValues values,  String selection,  String[] selectionArgs) {
        int rowsUpdated = 0;

        switch(uriMatcher.match(uri)){
            case uriCode:
                rowsUpdated = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: "+uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE);
            onCreate(db);
        }
    }
}
