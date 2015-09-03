package com.jeonsoft.facebundypro.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WendellWayne on 5/12/2015.
 */
public class EmployeeDataSource {
    private static EmployeeDataSource instance;
    private Context context;
    private SQLiteDatabase db;
    private FaceBundySQLiteHelper dbHelper;
    private String[] allColumns = {
            FaceBundySQLiteHelper.COLUMN_EMPLOYEES_ID,
            FaceBundySQLiteHelper.COLUMN_EMPLOYEES_ACCESS_CODE,
            FaceBundySQLiteHelper.COLUMN_EMPLOYEES_NAME,
            FaceBundySQLiteHelper.COLUMN_EMPLOYEES_NICKNAME,
            FaceBundySQLiteHelper.COLUMN_EMPLOYEES_LASTNAME,
            FaceBundySQLiteHelper.COLUMN_EMPLOYEES_FIRSTNAME
    };

    private EmployeeDataSource(Context context) {
        this.context = context;
        dbHelper = new FaceBundySQLiteHelper(context);
    }

    public static EmployeeDataSource getInstance(Context context) {
        if (instance == null)
            instance = new EmployeeDataSource(context);
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

    public int createEmployee(String accesscode, String name, String nickName, String lastName, String firstName) {
        ContentValues values = new ContentValues();
        values.put(FaceBundySQLiteHelper.COLUMN_EMPLOYEES_ACCESS_CODE, accesscode);
        values.put(FaceBundySQLiteHelper.COLUMN_EMPLOYEES_NAME, name);
        values.put(FaceBundySQLiteHelper.COLUMN_EMPLOYEES_NICKNAME, nickName);
        values.put(FaceBundySQLiteHelper.COLUMN_EMPLOYEES_LASTNAME, lastName);
        values.put(FaceBundySQLiteHelper.COLUMN_EMPLOYEES_FIRSTNAME, firstName);
        int id = (int) db.insert(FaceBundySQLiteHelper.TABLE_EMPLOYEES, "null", values);
        return id;
    }

    public void clear() {
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_EMPLOYEES);
    }

    public void delete(int id) {
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_EMPLOYEES + " WHERE " + FaceBundySQLiteHelper.COLUMN_EMPLOYEES_ID + " = " + String.valueOf(id));
    }

    public void deleteByAccessCode(String accessCode) {
        db.execSQL("DELETE FROM " + FaceBundySQLiteHelper.TABLE_EMPLOYEES + " WHERE " + FaceBundySQLiteHelper.COLUMN_EMPLOYEES_ACCESS_CODE + " = '" + accessCode + "'");
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<Employee>();

        Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_EMPLOYEES, allColumns, null, null, null, null, FaceBundySQLiteHelper.COLUMN_EMPLOYEES_NAME + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Employee log = cursorToEmployee(cursor);
            employees.add(log);
            cursor.moveToNext();
        }
        cursor.close();
        return employees;
    }

    public List<Employee> getAllEmployeesLike(String accessCode) {
        List<Employee> employees = new ArrayList<Employee>();

        //Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_EMPLOYEES, allColumns, FaceBundySQLiteHelper.COLUMN_EMPLOYEES_ACCESS_CODE + " LIKE ?", new String[] { accessCode }, null, null, FaceBundySQLiteHelper.COLUMN_EMPLOYEES_NAME + " ASC");
        Cursor cursor = db.rawQuery("SELECT id, accesscode, name, nickname, lastname, firstname FROM employees WHERE accesscode LIKE '%" + accessCode + "%' ORDER BY name ASC", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Employee log = cursorToEmployee(cursor);
            employees.add(log);
            cursor.moveToNext();
        }
        cursor.close();
        return employees;
    }

    public Employee getEmployee(String accessCode) {
        Employee employee = null;

        Cursor cursor = db.query(FaceBundySQLiteHelper.TABLE_EMPLOYEES, allColumns, FaceBundySQLiteHelper.COLUMN_EMPLOYEES_ACCESS_CODE + " = ?", new String[] { accessCode }, null, null, FaceBundySQLiteHelper.COLUMN_EMPLOYEES_NAME + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            employee = cursorToEmployee(cursor);
            cursor.moveToNext();
        }
        cursor.close();

        return employee;
    }

    private Employee cursorToEmployee(Cursor cursor) {
        Employee ft = new Employee(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
        return ft;
    }
}
