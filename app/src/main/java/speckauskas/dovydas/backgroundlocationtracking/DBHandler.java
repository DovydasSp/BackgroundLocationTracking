package speckauskas.dovydas.backgroundlocationtracking;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    //INFORMATION OF DB
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "locationsDB.db";
    public static final String TABLE_NAME = "Locations";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_TRIP_ID = "TripID";
    public static final String COLUMN_DATETIME = "Datetime";
    public static final String COLUMN_NAME_LATITUDE = "Latitude";
    public static final String COLUMN_NAME_LONGITUDE = "Longitude";

    //INITIALIZE DB
    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    //CREATE DB TABLE
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TRIP_ID + " INTEGER, " +
                COLUMN_DATETIME + " TEXT, " +
                COLUMN_NAME_LATITUDE + " DOUBLE, "+
                COLUMN_NAME_LONGITUDE + " DOUBLE)";
        db.execSQL(CREATE_TABLE);
        Log.i("DBHandler", "DB created.");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {}

    //LOAD DB VALUES INTO STRING
    public String loadHandler() {
        String result = "";
        String query = "Select * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            int result_0 = cursor.getInt(0);
            int result_1 = cursor.getInt(1);
            String result_2 = cursor.getString(2);
            double result_3 = cursor.getDouble(3);
            double result_4 = cursor.getDouble(4);
            result += String.valueOf(result_0) + " " + String.valueOf(result_1) + " " +
                    String.valueOf(result_2) + " " + String.valueOf(result_3) + " " +
                    String.valueOf(result_4) + " " + System.getProperty("line.separator");
        }
        cursor.close();
        db.close();
        return result;
    }

    //ADD TRIP COORDINATES TO DB
    public void addHandler(DBClass location) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATETIME, location.getDateTime());
        values.put(COLUMN_TRIP_ID, location.getTripID());
        values.put(COLUMN_NAME_LATITUDE, location.getLatitude());
        values.put(COLUMN_NAME_LONGITUDE, location.getLongitude());
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    //GET ALL STORED TRIP COORDINATES
    public List<DBClass> findHandler(int tripID) {
        String query = "Select * FROM " + TABLE_NAME + " WHERE " + COLUMN_TRIP_ID + " = " + "'" + tripID + "';";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<DBClass> locationList = new ArrayList<DBClass>();
        if (cursor != null) {
            cursor.moveToFirst();
            for(int i = 0; i < cursor.getCount(); i++)
            {
                DBClass location = new DBClass();
                location.setID(cursor.getInt(0));
                location.setTripID(cursor.getInt(1));
                location.setDateTime(cursor.getString(2));
                location.setLatitude(cursor.getDouble(3));
                location.setLongitude(cursor.getDouble(4));
                locationList.add(location);
                cursor.moveToNext();
            }
            cursor.close();
        } else {
            locationList = null;
        }
        db.close();
        return locationList;
    }

    //GET LAST STORED TRIP
    public int findLastTripHandler() {
        String query = "Select " + COLUMN_TRIP_ID + " FROM " + TABLE_NAME + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int lastTrip = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            for(int i = 0; i < cursor.getCount(); i++)
            {
                int thisTrip = cursor.getInt(0);
                if(thisTrip > lastTrip)
                    lastTrip = thisTrip;
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return lastTrip;
    }

    //DUMP TABLE VALUES
    public void clearTableHandler() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
        db.execSQL("vacuum");
        db.close();
    }
}