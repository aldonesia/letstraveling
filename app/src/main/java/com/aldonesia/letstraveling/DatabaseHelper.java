package com.aldonesia.letstraveling;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by root on 10/12/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "letstraveling.db";
    public static final String TABLE_NAME = "place_table";
    public static final String COL_1 = "place_id";
    public static final String COL_2 = "place_name";
    public static final String COL_3 = "place_vicinity";
    public static final String COL_4 = "place_lat";
    public static final String COL_5 = "place_lng";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" (ID INTEGER PRIMARY KEY AUTOINCREMENT, PLACE_NAME TEXT, PLACE_VICINITY TEXT, PLACE_LAT REAL, PLACE_LNG REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS  " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertdata(String place_name, String place_vicinity, Double place_lat, Double place_lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, place_name);
        contentValues.put(COL_3, place_vicinity);
        contentValues.put(COL_4, place_lat);
        contentValues.put(COL_5, place_lng);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public Cursor GetAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " +TABLE_NAME, null );
        return res;
    }

    public boolean updatedata(String place_id, String place_name, String place_vicinity, Double place_lat, Double place_lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, place_id);
        contentValues.put(COL_2, place_name);
        contentValues.put(COL_3, place_vicinity);
        contentValues.put(COL_4, place_lat);
        contentValues.put(COL_5, place_lng);

        db.update(TABLE_NAME, contentValues, "place_id = ?",new String[] { place_id });
        return true;
    }

    public Integer deletedata(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
    }

    public boolean deleteAlldata(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        return true;
    }
}
