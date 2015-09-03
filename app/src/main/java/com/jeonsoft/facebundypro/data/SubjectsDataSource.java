package com.jeonsoft.facebundypro.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WendellWayne on 3/14/2015.
 */
public final class SubjectsDataSource {
    private static SubjectsDataSource instance;
    private Context context;
    private SQLiteDatabase db;
    private FaceBundySQLiteHelper dbHelper;
    private String[] allColumns = {
            FaceBundySQLiteHelper.COLUMN_SUBJECTS_ID,
            FaceBundySQLiteHelper.COLUMN_SUBJECTS_SUBJECT_ID,
            FaceBundySQLiteHelper.COLUMN_SUBJECTS_TIMESTAMP,
            FaceBundySQLiteHelper.COLUMN_SUBJECTS_TEMPLATE,
            FaceBundySQLiteHelper.COLUMN_SUBJECTS_THUMBNAIL
    };

    private SubjectsDataSource(Context context) {
        this.context = context;
        dbHelper = new FaceBundySQLiteHelper(context);
    }

    public static SubjectsDataSource getInstance(Context context) {
        if (instance == null)
            instance = new SubjectsDataSource(context);
        return instance;
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public boolean isOpen() {
        return db.isOpen();
    }

    public void close() {
        dbHelper.close();
    }

    public int createSubject(String accesscode, String timestamp, byte[] template, byte[] photo) {
        ContentValues values = new ContentValues();
        values.put(FaceBundySQLiteHelper.COLUMN_SUBJECTS_SUBJECT_ID, accesscode);
        values.put(FaceBundySQLiteHelper.COLUMN_SUBJECTS_TIMESTAMP, timestamp);
        values.put(FaceBundySQLiteHelper.COLUMN_SUBJECTS_TEMPLATE, template);
        values.put(FaceBundySQLiteHelper.COLUMN_SUBJECTS_THUMBNAIL, photo);

        int id = (int) db.insert(FaceBundySQLiteHelper.TABLE_SUBJECTS, "null", values);
        return id;
    }

    public void clear() {
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_SUBJECTS);
    }

    public void delete(int id) {
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_SUBJECTS + " WHERE " + FaceBundySQLiteHelper.COLUMN_SUBJECTS_ID + " = " + String.valueOf(id));
    }

    public void deleteByAccessCode(String accessCode) {
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_SUBJECTS + " WHERE " + FaceBundySQLiteHelper.COLUMN_SUBJECTS_SUBJECT_ID + " = '" + accessCode + "'");
    }

    public List<Subject> getAllSubjects() {
        List<Subject> faces = new ArrayList<Subject>();

        Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_SUBJECTS, allColumns, null, null, null, null, FaceBundySQLiteHelper.COLUMN_SUBJECTS_SUBJECT_ID + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Subject log = cursorToSubject(cursor);
            faces.add(log);
            cursor.moveToNext();
        }
        cursor.close();
        return faces;
    }

    public Subject getSubject(String accessCode) {
        Subject subject = null;

        Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_SUBJECTS, allColumns, FaceBundySQLiteHelper.COLUMN_SUBJECTS_SUBJECT_ID + " = ?", new String[] { accessCode }, null, null, FaceBundySQLiteHelper.COLUMN_SUBJECTS_SUBJECT_ID + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            subject = cursorToSubject(cursor);
            cursor.moveToNext();
        }
        cursor.close();

        return subject;
    }

    private Subject cursorToSubject(Cursor cursor) {
        Subject ft = new Subject(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getBlob(3), cursor.getBlob(4));
        return ft;
    }
}
