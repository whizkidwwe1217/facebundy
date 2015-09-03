package com.jeonsoft.facebundypro.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jeonsoft.facebundypro.logging.Logger;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by WendellWayne on 6/26/2015.
 */
public final class ItineraryProjectDataSource {
    private static ItineraryProjectDataSource instance;
    private Context context;
    private SQLiteDatabase db;
    private FaceBundySQLiteHelper dbHelper;
    private String[] allColumns = {
            FaceBundySQLiteHelper.COLUMN_ITINERARY_ID,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_ACCESS_CODE,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_PROJECT,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_TIME_IN,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_TIME_OUT,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_LATITUDE,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_LONGITUDE,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_LOCATION,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_STATUS,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_DATECREATED
    };

    private ItineraryProjectDataSource(Context context) {
        this.context = context;
        dbHelper = new FaceBundySQLiteHelper(this.context);
    }

    public static ItineraryProjectDataSource getInstance(Context context) {
        if (instance == null)
            instance = new ItineraryProjectDataSource(context);
        return instance;
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public ItineraryProject createItinerary(String accesscode, String project, String timeIn, String timeOut, double latitude, double longitude, String location) {
        ContentValues values = new ContentValues();
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_ACCESS_CODE, accesscode);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_PROJECT, project);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TIME_IN, timeIn);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TIME_OUT, timeOut);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_LATITUDE, latitude);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_LONGITUDE, longitude);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_LOCATION, location);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_STATUS, 1);
        String dateCreated = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(Calendar.getInstance().getTime());
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_DATECREATED, dateCreated);
        int id = (int) db.insert(FaceBundySQLiteHelper.TABLE_ITINERARY, "null", values);
        ItineraryProject item = new ItineraryProject(id, accesscode, project, timeIn, timeOut, latitude, longitude, location, dateCreated);
        return item;
    }

    public ItineraryProject updateItinerary(String accesscode, String project, String timeIn, String timeOut, double latitude, double longitude, String location, final int referenceId) {
        ContentValues values = new ContentValues();
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_ACCESS_CODE, accesscode);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_PROJECT, project);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TIME_IN, timeIn);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TIME_OUT, timeOut);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_LATITUDE, latitude);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_LONGITUDE, longitude);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_LOCATION, location);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_STATUS, 2);
        int id = (int) db.update(FaceBundySQLiteHelper.TABLE_ITINERARY, values, "(Id = ?)", new String[] {String.valueOf(referenceId)});
        ItineraryProject item = new ItineraryProject(referenceId, accesscode, project, timeIn, timeOut, latitude, longitude, location);
        return item;
    }

    public void beginTransaction() {
        db.beginTransaction();
    }

    public void setTransactionSuccessful() {
        db.setTransactionSuccessful();
    }

    public void endTransaction() {
        db.endTransaction();
    }
    public boolean isOpen() {
        return db.isOpen();
    }
    public void clearItinerary() {
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_ITINERARY);
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_ITINERARY_TASKS);
    }

    public void clearItineraryByAccessCode(final String accessCode) {
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_ITINERARY + " WHERE " + FaceBundySQLiteHelper.COLUMN_ITINERARY_ACCESS_CODE + " = '" + accessCode + "'");
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_ITINERARY_TASKS + " WHERE " + FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_ACCESS_CODE + " = '" + accessCode + "'");
    }

    public void deleteById(int id) {
        Logger.logE("DELETE FROM " + FaceBundySQLiteHelper.TABLE_ITINERARY + " WHERE " + FaceBundySQLiteHelper.COLUMN_ITINERARY_ID + " = " + String.valueOf(id));
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_ITINERARY + " WHERE " + FaceBundySQLiteHelper.COLUMN_ITINERARY_ID + " = " + String.valueOf(id));
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_ITINERARY_TASKS + " WHERE " + FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_PROJECT_ID + " = " + String.valueOf(id));
    }

    public List<ItineraryProject> getAllItinerary() {
        List<ItineraryProject> itineraries = new ArrayList<ItineraryProject>();

        Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_ITINERARY, allColumns, null, null, null, null, FaceBundySQLiteHelper.COLUMN_ITINERARY_TIME_IN + " DESC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ItineraryProject log = cursorToItinerary(cursor);
            itineraries.add(log);
            cursor.moveToNext();
        }
        cursor.close();
        return itineraries;
    }

    public List<ItineraryProject> getAllItineraryByAccessCode(final String accessCode) {
        List<ItineraryProject> itineraries = new ArrayList<ItineraryProject>();

        Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_ITINERARY, allColumns, "(accesscode = ?)",  new String[] { accessCode }, null, null, FaceBundySQLiteHelper.COLUMN_ITINERARY_TIME_IN + " DESC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ItineraryProject log = cursorToItinerary(cursor);
            itineraries.add(log);
            cursor.moveToNext();
        }
        cursor.close();
        return itineraries;
    }

    public ArrayList<ItineraryProject> getUnCompletedTasks(String accessCode) {
        ArrayList<ItineraryProject> itineraries = new ArrayList<ItineraryProject>();

        Cursor cursor = db.rawQuery("SELECT id, accesscode, project, timeIn, timeOut, latitude, longitude, location, status, date_created FROM itinerary WHERE timeout IS NULL OR timeout = '' AND accesscode = '" + accessCode + "' ORDER BY timein DESC", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ItineraryProject project = cursorToItinerary(cursor);
            itineraries.add(project);
            cursor.moveToNext();
        }
        cursor.close();
        return itineraries;
    }

    private ItineraryProject cursorToItinerary(Cursor cursor) {
        ItineraryProject tl = new ItineraryProject(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5), cursor.getDouble(6), cursor.getString(7), cursor.getString(9));
        tl.setStatus(cursor.getInt(8));
        return tl;
    }
}



