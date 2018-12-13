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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.danie.runningtracker2.Util;

import org.json.JSONObject;

import java.util.HashMap;

public class TracksProvider extends ContentProvider {
    private static final String TAG = "TracksProvider";
    public static final String PROVIDER_NAME = "com.example.danie.runningtracker2.ContentProviders.TracksProvider";
    public static final String PATH = "TracksProvider";
    public static final String URL = "content://"+PROVIDER_NAME+"/"+PATH;
    public static final Uri CONTENT_URL = Uri.parse(URL);
    public static final String MIME_TYPE = "vnd.android.cursor.dir"+"/"+PATH;

    //fields
    public static final String ID = "id";
    public static final String JSON_OBJECT = "json_object";
    private static final int URI_CODE = 1;
    private static HashMap<String, String> values;

    private static UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, PATH, URI_CODE);
    }

    private SQLiteDatabase db;
    public static final String DATABASE_NAME = "tracksDB";
    public static final String TABLE_NAME = "tracksTABLE";
    public static final int DATABASE_VERSION = 1;
    public static final String CREATE_DB_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                    + "("
                    + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + JSON_OBJECT + " TEXT NOT NULL"
                    + ");";
    public static final String DROP_DB_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    @Override
    public boolean onCreate() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        db = dbHelper.getWritableDatabase();

        return db!=null;
    }

    
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TABLE_NAME);

        switch(uriMatcher.match(uri)){
            case URI_CODE:
                queryBuilder.setProjectionMap(values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }

        Cursor c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    
    @Override
    public String getType(@NonNull Uri uri) {
        switch(uriMatcher.match(uri)){
            case URI_CODE:
                return MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI "+uri);
        }
    }


    @Override
    public Uri insert(@NonNull Uri uri,  ContentValues values) {
        long rowID = db.insert(TABLE_NAME, null, values);

        if(rowID>0){
            Uri _uri = ContentUris.withAppendedId(CONTENT_URL, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);

            return _uri;
        }else{
            Log.d(TAG, "insert: Failed");
            return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri,  String selection,  String[] selectionArgs) {
        int rowsDeleted = 0;

        switch(uriMatcher.match(uri)){
            case URI_CODE:
                rowsDeleted = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri,  ContentValues values,  String selection,  String[] selectionArgs) {
        int rowsUpdated = 0;

        switch(uriMatcher.match(uri)){
            case URI_CODE:
                rowsUpdated = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper{

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_DB_TABLE);
            onCreate(db);
        }
    }
}
