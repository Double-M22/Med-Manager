package com.cyclon.com.med_manager.Data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.cyclon.com.med_manager.Constants.DataConstants;

public class DatabaseHelper extends SQLiteOpenHelper{

    private static  String medication_table_name;

    public DatabaseHelper(Context context) {
        super(context, DataConstants.MEDICATION_DATABASE, null, 1);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String profile_email = sp.getString(DataConstants.PROFILE_EMAIL, null);
        if (profile_email != null && profile_email.contains("@")){
            medication_table_name = profile_email.substring(0, profile_email.indexOf("@"));
            medication_table_name = medication_table_name + "_" + profile_email.substring(profile_email.indexOf("@")+1, profile_email.indexOf("."));
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(DataConstants.MEDICATION_TABLE_NAME, medication_table_name);
            editor.apply();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String CREATE_MEDICATION_TABLE = " CREATE TABLE " + medication_table_name + " ( " +
                DataConstants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DataConstants.MEDICATION_NAME + " TEXT NOT NULL, " +
                DataConstants.MEDICATION_DESCRIPTION + " TEXT NOT NULL, " +
                DataConstants.MEDICATION_INTERVAL + " TEXT NOT NULL, " +
                DataConstants.MEDICATION_START_DATE + " TEXT NOT NULL, " +
                DataConstants.MEDICATION_END_DATE + " TEXT NOT NULL " + "); ";

        final String CREATE_PROFILE_TABLE = " CREATE TABLE " + DataConstants.PROFILE_TABLE + " ( " +
                DataConstants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DataConstants.PROFILE_NAME + " TEXT NOT NULL, " +
                DataConstants.PROFILE_EMAIL + " TEXT NOT NULL, " +
                DataConstants.PROFILE_IMAGE + "  BLOB " + "); ";

        sqLiteDatabase.execSQL(CREATE_MEDICATION_TABLE);
        sqLiteDatabase.execSQL(CREATE_PROFILE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + medication_table_name);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DataConstants.PROFILE_TABLE);
        onCreate(sqLiteDatabase);
    }

    public boolean addMedication(String ... values){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DataConstants.MEDICATION_NAME, values[0]);
        cv.put(DataConstants.MEDICATION_DESCRIPTION, values[1]);
        cv.put(DataConstants.MEDICATION_INTERVAL, values[2]);
        cv.put(DataConstants.MEDICATION_START_DATE, values[3]);
        cv.put(DataConstants.MEDICATION_END_DATE, values[4]);

        long result = db.insert(medication_table_name, null, cv);
        return !(result == -1);
    }

    public boolean addProfile(String name, String email, byte[] image){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DataConstants.PROFILE_NAME, name);
        cv.put(DataConstants.PROFILE_EMAIL, email);
        cv.put(DataConstants.PROFILE_IMAGE, image);

        long result = db.insert(DataConstants.PROFILE_TABLE, null, cv);
        return !(result == -1);
    }

    public boolean updateProfile(String old_name, String new_name, String email, byte[] image){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DataConstants.PROFILE_NAME, new_name);
        cv.put(DataConstants.PROFILE_EMAIL, email);
        cv.put(DataConstants.PROFILE_IMAGE, image);

        long result = db.update(DataConstants.PROFILE_TABLE, cv, DataConstants.PROFILE_NAME+" = ?", new String[]{old_name});
        return !(result == -1);
    }

    public int removeMedication(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(medication_table_name, DataConstants.ID+" = ?", new String[]{id});
    }

    public Cursor getMedications(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(" select * from " + medication_table_name, null);
    }

    public Cursor getProfile(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(" select * from " + DataConstants.PROFILE_TABLE, null);
    }
}
