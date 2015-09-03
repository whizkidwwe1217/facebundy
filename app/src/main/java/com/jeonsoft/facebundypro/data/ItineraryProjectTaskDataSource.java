package com.jeonsoft.facebundypro.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jeonsoft.facebundypro.logging.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WendellWayne on 6/30/2015.
 */
public final class ItineraryProjectTaskDataSource {
    private static ItineraryProjectTaskDataSource instance;
    private Context context;
    private SQLiteDatabase db;
    private FaceBundySQLiteHelper dbHelper;
    private String[] allColumns = {
            FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_ID,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_ACCESS_CODE,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_TIME_IN,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_TASK,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_NOTES,
            FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_PROJECT_ID

    };

    private ItineraryProjectTaskDataSource(Context context) {
        this.context = context;
        dbHelper = new FaceBundySQLiteHelper(this.context);
    }

    public static ItineraryProjectTaskDataSource getInstance(Context context) {
        if (instance == null)
            instance = new ItineraryProjectTaskDataSource(context);
        return instance;
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public ItineraryProjectTask createItineraryTask(String accessCode, String timeIn, String task, String notes, int projectId) {
        ContentValues values = new ContentValues();
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_ACCESS_CODE, accessCode);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_TIME_IN, timeIn);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_TASK, task);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_NOTES, notes);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_PROJECT_ID, projectId);
        int id = (int) db.insert(FaceBundySQLiteHelper.TABLE_ITINERARY_TASKS, "null", values);
        ItineraryProjectTask item = new ItineraryProjectTask(id, accessCode, timeIn, task, notes, projectId);
        return item;
    }

    public ItineraryProjectTask updateItineraryTask(String accessCode, String timeIn, String task, String notes, int projectId, final int referenceId) {
        ContentValues values = new ContentValues();
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_ACCESS_CODE, accessCode);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_TIME_IN, timeIn);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_TASK, task);
        values.put(FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_NOTES, notes);
        int id = (int) db.update(FaceBundySQLiteHelper.TABLE_ITINERARY, values, "(Id = ?)", new String[] {String.valueOf(referenceId)});
        ItineraryProjectTask item = new ItineraryProjectTask(id, accessCode, timeIn,  task, notes, projectId);
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
    public void clearItineraryTask() {
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_ITINERARY_TASKS);
    }


    public void deleteById(int id) {
        Logger.logE("DELETE FROM " + FaceBundySQLiteHelper.TABLE_ITINERARY_TASKS + " WHERE " + FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_ID + " = " + String.valueOf(id));
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_ITINERARY_TASKS + " WHERE " + FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_ID + " = " + String.valueOf(id));
    }

    public List<ItineraryProjectTask> getAllItineraryTasks() {
        List<ItineraryProjectTask> itineraries = new ArrayList<ItineraryProjectTask>();

        Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_ITINERARY_TASKS, allColumns, null, null, null, null, FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_ID + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ItineraryProjectTask log = cursorToItineraryTask(cursor);
            itineraries.add(log);
            cursor.moveToNext();
        }
        cursor.close();
        return itineraries;
    }

    public List<ItineraryProjectTask> getAllItineraryTasksByProject(int projectId) {
        List<ItineraryProjectTask> itineraries = new ArrayList<ItineraryProjectTask>();

        //Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_ITINERARY_TASKS, allColumns, null, null, null, null, FaceBundySQLiteHelper.COLUMN_ITINERARY_TASKS_ID + " ASC");
        Cursor cursor = db.rawQuery("SELECT id, access_code, time_in, task, notes, project_id FROM itinerary_tasks WHERE project_id = " + String.valueOf(projectId) + " ORDER BY id ASC", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ItineraryProjectTask log = cursorToItineraryTask(cursor);
            itineraries.add(log);
            cursor.moveToNext();
        }
        cursor.close();
        return itineraries;
    }

    private ItineraryProjectTask cursorToItineraryTask(Cursor cursor) {
        ItineraryProjectTask tl = new ItineraryProjectTask(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5));
        return tl;
    }
}