package com.jeonsoft.facebundypro.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.settings.GlobalConstants;

/**
 * Created by Wendell Wayne on 1/13/2015.
 */
public class FaceBundySQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = GlobalConstants.DATABASE_NAME;
    private static final String TEXT_TYPE = " TEXT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String INT_TYPE = " INTEGER";
    private static final String BIG_INT_TYPE = " BIGINT";
    private static final String DATETIME_TYPE = " DATETIME";
    private static final String COMMA_SEP = ",";

    public static final String TABLE_TIMELOGS = "timelogs";
    public static final String COLUMN_TIMELOGS_ID = "id";
    public static final String COLUMN_TIMELOGS_ACCESS_CODE = "accesscode";
    public static final String COLUMN_TIMELOGS_TIME = "time";
    public static final String COLUMN_TIMELOGS_TYPE = "type";
    public static final String COLUMN_TIMELOGS_FILENAME = "filename";
    public static final String COLUMN_TIMELOGS_EDITION = "edition";
    public static final String COLUMN_TIMELOGS_LATITUDE = "latitude";
    public static final String COLUMN_TIMELOGS_LONGITUDE = "longitude";

    public static final String TABLE_EMPLOYEES = "employees";
    public static final String COLUMN_EMPLOYEES_ID = "id";
    public static final String COLUMN_EMPLOYEES_ACCESS_CODE = "accesscode";
    public static final String COLUMN_EMPLOYEES_NAME = "name";
    public static final String COLUMN_EMPLOYEES_NICKNAME = "nickname";
    public static final String COLUMN_EMPLOYEES_LASTNAME = "lastname";
    public static final String COLUMN_EMPLOYEES_FIRSTNAME = "firstname";

    private static final String SQL_CREATE_EMPLOYEES =
            "CREATE TABLE " + TABLE_EMPLOYEES + " (" +
                    COLUMN_EMPLOYEES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_EMPLOYEES_ACCESS_CODE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_EMPLOYEES_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_EMPLOYEES_NICKNAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_EMPLOYEES_FIRSTNAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_EMPLOYEES_LASTNAME + TEXT_TYPE + " )";
    private static final String SQL_DELETE_EMPLOYEES =
            "DROP TABLE IF EXISTS " + TABLE_EMPLOYEES;

    private static final String SQL_CREATE_LOGS =
            "CREATE TABLE " + TABLE_TIMELOGS + " (" +
                    COLUMN_TIMELOGS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TIMELOGS_ACCESS_CODE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_TIMELOGS_TIME + DATETIME_TYPE + COMMA_SEP +
                    COLUMN_TIMELOGS_TYPE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_TIMELOGS_FILENAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_TIMELOGS_EDITION + TEXT_TYPE + COMMA_SEP +
                    COLUMN_TIMELOGS_LATITUDE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_TIMELOGS_LONGITUDE + TEXT_TYPE + " )";
    private static final String SQL_DELETE_LOGS =
            "DROP TABLE IF EXISTS " + TABLE_TIMELOGS;

    public static final String TABLE_FACES = "faces";
    public static final String COLUMN_FACES_ID = "id";
    public static final String COLUMN_FACES_ACCESS_CODE = "accesscode";
    public static final String COLUMN_FACES_FILENAME = "filename";

    private static final String SQL_CREATE_FACES =
            "CREATE TABLE " + TABLE_FACES + " (" +
                    COLUMN_FACES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TIMELOGS_ACCESS_CODE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_TIMELOGS_FILENAME + TEXT_TYPE + " )";
    private static final String SQL_DELETE_FACES =
            "DROP TABLE IF EXISTS " + TABLE_FACES;

    public static final String TABLE_SUBJECTS = "Subjects";
    public static final String COLUMN_SUBJECTS_ID = "Id";
    public static final String COLUMN_SUBJECTS_SUBJECT_ID = "SubjectId";
    public static final String COLUMN_SUBJECTS_TEMPLATE = "Template";
    public static final String COLUMN_SUBJECTS_THUMBNAIL = "Thumbnail";
    public static final String COLUMN_SUBJECTS_TIMESTAMP = "Timestamp";

    private static final String SQL_CREATE_SUBJECTS =
            "CREATE TABLE " + TABLE_SUBJECTS + " (" +
                    COLUMN_SUBJECTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_SUBJECTS_SUBJECT_ID + TEXT_TYPE + " NOT NULL UNIQUE " + COMMA_SEP +
                    COLUMN_SUBJECTS_TIMESTAMP + DATETIME_TYPE + " DEFAULT CURRENT_TIMESTAMP" + COMMA_SEP +
                    COLUMN_SUBJECTS_TEMPLATE + BLOB_TYPE + " NOT NULL " + COMMA_SEP +
                    COLUMN_SUBJECTS_THUMBNAIL + BLOB_TYPE + " )";
    private static final String SQL_DELETE_SUBJECTS =
            "DROP TABLE IF EXISTS " + TABLE_SUBJECTS;

    public static final String TABLE_ITINERARY = "itinerary";
    public static final String COLUMN_ITINERARY_ID = "id";
    public static final String COLUMN_ITINERARY_PROJECT = "project";
    public static final String COLUMN_ITINERARY_ACCESS_CODE = "accesscode";
    public static final String COLUMN_ITINERARY_TIME_IN = "timeIn";
    public static final String COLUMN_ITINERARY_TIME_OUT = "timeOut";
    public static final String COLUMN_ITINERARY_LATITUDE = "latitude";
    public static final String COLUMN_ITINERARY_LONGITUDE = "longitude";
    public static final String COLUMN_ITINERARY_LOCATION = "location";
    public static final String COLUMN_ITINERARY_STATUS = "status";
    public static final String COLUMN_ITINERARY_DATECREATED = "date_created";

    private static final String SQL_CREATE_ITINERARY =
            "CREATE TABLE " + TABLE_ITINERARY + " (" +
                    COLUMN_ITINERARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_ITINERARY_ACCESS_CODE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_PROJECT + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_TIME_IN + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_TIME_OUT + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_LATITUDE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_LONGITUDE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_LOCATION + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_STATUS + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_DATECREATED + INT_TYPE + " )";
    private static final String SQL_DELETE_ITINERARY =
            "DROP TABLE IF EXISTS " + TABLE_ITINERARY;

    public static final String TABLE_ITINERARY_TASKS = "itinerary_tasks";
    public static final String COLUMN_ITINERARY_TASKS_ID = "id";
    public static final String COLUMN_ITINERARY_TASKS_ACCESS_CODE = "access_code";
    public static final String COLUMN_ITINERARY_TASKS_TIME_IN = "time_in";
    public static final String COLUMN_ITINERARY_TASKS_NOTES = "notes";
    public static final String COLUMN_ITINERARY_TASKS_TASK = "task";
    public static final String COLUMN_ITINERARY_TASKS_PROJECT_ID = "project_id";
    private static final String SQL_CREATE_ITINERARY_TASKS =
            "CREATE TABLE " + TABLE_ITINERARY_TASKS + " (" +
                    COLUMN_ITINERARY_TASKS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_ITINERARY_TASKS_ACCESS_CODE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_TASKS_TIME_IN + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_TASKS_TASK + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_TASKS_NOTES + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITINERARY_TASKS_PROJECT_ID + INT_TYPE + " )";
    private static final String SQL_DELETE_ITINERARY_TASKS =
            "DROP TABLE IF EXISTS " + TABLE_ITINERARY_TASKS;

    public FaceBundySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_LOGS);
        db.execSQL(SQL_CREATE_FACES);
        db.execSQL(SQL_CREATE_SUBJECTS);
        db.execSQL(SQL_CREATE_EMPLOYEES);
        db.execSQL(SQL_CREATE_ITINERARY);
        db.execSQL(SQL_CREATE_ITINERARY_TASKS);
        Logger.logE(db.getPath());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_LOGS);
        db.execSQL(SQL_DELETE_FACES);
        db.execSQL(SQL_DELETE_SUBJECTS);
        db.execSQL(SQL_DELETE_EMPLOYEES);
        db.execSQL(SQL_DELETE_ITINERARY);
        db.execSQL(SQL_DELETE_ITINERARY_TASKS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
