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
 * Created by Wendell Wayne on 1/13/2015.
 */
public final class TimelogDataSource {
    private static TimelogDataSource instance;
    private Context context;
    private SQLiteDatabase db;
    private FaceBundySQLiteHelper dbHelper;
    private String[] allColumns = {
            FaceBundySQLiteHelper.COLUMN_TIMELOGS_ID,
            FaceBundySQLiteHelper.COLUMN_TIMELOGS_ACCESS_CODE,
            FaceBundySQLiteHelper.COLUMN_TIMELOGS_TIME,
            FaceBundySQLiteHelper.COLUMN_TIMELOGS_TYPE,
            FaceBundySQLiteHelper.COLUMN_TIMELOGS_FILENAME,
            FaceBundySQLiteHelper.COLUMN_TIMELOGS_EDITION,
            FaceBundySQLiteHelper.COLUMN_TIMELOGS_LATITUDE,
            FaceBundySQLiteHelper.COLUMN_TIMELOGS_LONGITUDE,
    };

    private TimelogDataSource(Context context) {
        this.context = context;
        dbHelper = new FaceBundySQLiteHelper(this.context);
    }

    public static TimelogDataSource getInstance(Context context) {
        if (instance == null)
            instance = new TimelogDataSource(context);
        return instance;
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public TimeLog createTimeLog(String accesscode, String time, String type, String filename, int edition, double latitude, double longitude) {
        ContentValues values = new ContentValues();
        values.put(FaceBundySQLiteHelper.COLUMN_TIMELOGS_ACCESS_CODE, accesscode);
        values.put(FaceBundySQLiteHelper.COLUMN_TIMELOGS_TIME, time);
        values.put(FaceBundySQLiteHelper.COLUMN_TIMELOGS_TYPE, type);
        values.put(FaceBundySQLiteHelper.COLUMN_TIMELOGS_FILENAME, filename);
        values.put(FaceBundySQLiteHelper.COLUMN_TIMELOGS_EDITION, edition);
        values.put(FaceBundySQLiteHelper.COLUMN_TIMELOGS_LATITUDE, latitude);
        values.put(FaceBundySQLiteHelper.COLUMN_TIMELOGS_LONGITUDE, longitude);
        int id = (int) db.insert(FaceBundySQLiteHelper.TABLE_TIMELOGS, "null", values);

        TimeLog tl = new TimeLog(id, accesscode, time, type, filename, edition, latitude, longitude);
        return tl;
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

    public void clearTimeLogs() {
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_TIMELOGS);
    }

    public void deleteByFileName(String filename) {
        db.execSQL("DELETE FROM ".concat(FaceBundySQLiteHelper.TABLE_TIMELOGS).concat(" WHERE ").concat(FaceBundySQLiteHelper.COLUMN_TIMELOGS_FILENAME).concat(" = '").concat(filename).concat("'"));
    }

    public void deleteById(int id) {
        Logger.logE("DELETE FROM " + FaceBundySQLiteHelper.TABLE_TIMELOGS + " WHERE " + FaceBundySQLiteHelper.COLUMN_TIMELOGS_ID + " = " + String.valueOf(id));
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_TIMELOGS + " WHERE " + FaceBundySQLiteHelper.COLUMN_TIMELOGS_ID + " = " + String.valueOf(id));
    }

    public List<TimeLog> getAllTimeLogs() {
        List<TimeLog> timelogs = new ArrayList<TimeLog>();

        Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_TIMELOGS, allColumns, null, null, null, null, FaceBundySQLiteHelper.COLUMN_TIMELOGS_ID + " DESC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TimeLog log = cursorToTimeLog(cursor);
            timelogs.add(log);
            cursor.moveToNext();
        }
        cursor.close();
        return timelogs;
    }

    public TimeLog getLastTimeLogByAccessCode(String accessCode) {
        Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_TIMELOGS, allColumns, FaceBundySQLiteHelper.COLUMN_TIMELOGS_ACCESS_CODE + " = ?", new String[] { accessCode }, null, null, FaceBundySQLiteHelper.COLUMN_TIMELOGS_ID + " DESC LIMIT 1");
        cursor.moveToFirst();
        if (cursor == null)
            return null;
        TimeLog log = cursorToTimeLog(cursor);
        cursor.close();
        return log;
    }

    public TimeLog getTimeLogByFileName(String filename) {
        String query = "SELECT id, filename FROM ".concat(FaceBundySQLiteHelper.TABLE_TIMELOGS).concat(" WHERE ").concat(FaceBundySQLiteHelper.COLUMN_TIMELOGS_FILENAME).concat(" = '").concat(filename).concat("'");

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        if (cursor.getCount() <= 0)
            return null;
        TimeLog log = cursorToTimeLogIdentity(cursor);
        cursor.close();
        return log;
    }

    public List<TimeLog> getAllTimeLogsByAccessCode(String accessCode) {
        List<TimeLog> timelogs = new ArrayList<TimeLog>();

        Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_TIMELOGS, allColumns, FaceBundySQLiteHelper.COLUMN_TIMELOGS_ACCESS_CODE + " = ?", new String[] { accessCode }, null, null, FaceBundySQLiteHelper.COLUMN_TIMELOGS_ID + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TimeLog log = cursorToTimeLog(cursor);
            timelogs.add(log);
            cursor.moveToNext();
        }

        cursor.close();
        return timelogs;
    }

    private TimeLog cursorToTimeLog(Cursor cursor) {
        TimeLog tl = new TimeLog(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5), cursor.getDouble(6), cursor.getDouble(7));
        return tl;
    }

    private TimeLog cursorToTimeLogIdentity(Cursor cursor) {
        TimeLog tl = new TimeLog(cursor.getInt(0), cursor.getString(1), "", "", "", 1, 0, 0);
        return tl;
    }
}
