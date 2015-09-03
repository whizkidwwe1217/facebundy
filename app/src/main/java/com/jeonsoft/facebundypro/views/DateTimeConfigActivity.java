package com.jeonsoft.facebundypro.views;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.settings.GlobalConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by WendellWayne on 5/12/2015.
 */
public class DateTimeConfigActivity extends BaseActionBarActivity implements View.OnClickListener {
    private TextView tvHour, tvMinutes, tvAMPM, tvDayMonth, tvYear, tvSecond;
    private LinearLayout llTime, llDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_datetime);

        tvHour = (TextView) findViewById(R.id.tvHour);
        tvMinutes = (TextView) findViewById(R.id.tvMinutes);
        tvAMPM = (TextView) findViewById(R.id.tvAMPM);
        tvDayMonth = (TextView) findViewById(R.id.tvDayMonth);
        tvYear = (TextView) findViewById(R.id.tvYear);
        tvSecond = (TextView) findViewById(R.id.tvSecond);

        llTime = (LinearLayout) findViewById(R.id.llTime);
        llDate = (LinearLayout) findViewById(R.id.llDate);

        tvHour.setTag(0);
        tvMinutes.setTag(0);
        tvAMPM.setTag(0);
        llTime.setTag(0);
        tvHour.setOnClickListener(this);
        tvMinutes.setOnClickListener(this);
        tvAMPM.setOnClickListener(this);
        llTime.setOnClickListener(this);

        tvDayMonth.setTag(1);
        tvYear.setTag(1);
        llDate.setTag(1);
        tvDayMonth.setOnClickListener(this);
        tvYear.setOnClickListener(this);
        llDate.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDateTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timerTask != null)
            timerTask.cancel();
    }

    private void displayDateTime() {
        Calendar calendar = getTime();
        final int year = calendar.get(Calendar.YEAR);
        //MM/dd/yyyy hh:mm:ss a
        SimpleDateFormat df = new SimpleDateFormat("dd MMM");
        SimpleDateFormat df2 = new SimpleDateFormat("a");
        tvDayMonth.setText(df.format(calendar.getTime()));
        tvYear.setText(String.valueOf(year));
        tvHour.setText(new SimpleDateFormat("hh").format(calendar.getTime()));
        tvMinutes.setText(":" + new SimpleDateFormat("mm").format(calendar.getTime()));
        tvAMPM.setText(df2.format(calendar.getTime()));
        tvSecond.setText(new SimpleDateFormat("ss").format(calendar.getTime()));
    }

    private TimerTask timerTask;

    private void showDateTime() {
        final Handler handler = new Handler();
        Timer callTimer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        displayDateTime();
                    }
                });
            }
        };
        callTimer.schedule(timerTask, 0, 1000);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            int tag = (int) v.getTag();
            final Calendar calendar = getTime();
            final int hour = calendar.get(Calendar.HOUR_OF_DAY);
            final int minute = calendar.get(Calendar.MINUTE);
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            final CacheManager cm = CacheManager.getInstance(this);

            if(tag == 0) {
                final TimePickerDialog tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i2) {
                        long serverTime, elapsedTime;
                        Calendar cal = calendar;
                        cal.set(Calendar.HOUR, i);
                        cal.set(Calendar.MINUTE, i2);
                        cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                        cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                        cal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
                        elapsedTime = SystemClock.elapsedRealtime();
                        serverTime = cal.getTimeInMillis();
                        cm.setPreference(CacheManager.SERVER_TIME, serverTime);
                        cm.setPreference(CacheManager.ELAPSED_TIME, elapsedTime);
                        cm.setPreference(CacheManager.TIME_LAST_SYNC_SOURCE, GlobalConstants.MANUAL_SYNC_SOURCE);
                        displayDateTime();
                        Toast.makeText(DateTimeConfigActivity.this, "Time was modified.", Toast.LENGTH_SHORT).show();
                    }
                }, hour, minute, false);
                tpd.show(getFragmentManager(), "timePicker");
            } else {
                final DatePickerDialog dpd = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog, int i, int i2, int i3) {
                        long serverTime, elapsedTime;
                        Calendar cal = calendar;
                        cal.set(Calendar.YEAR, i);
                        cal.set(Calendar.MONTH, i2);
                        cal.set(Calendar.DAY_OF_MONTH, i3);
                        cal.set(Calendar.HOUR, calendar.get(Calendar.HOUR));
                        cal.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
                        elapsedTime = SystemClock.elapsedRealtime();
                        serverTime = cal.getTimeInMillis();
                        cm.setPreference(CacheManager.SERVER_TIME, serverTime);
                        cm.setPreference(CacheManager.ELAPSED_TIME, elapsedTime);
                        displayDateTime();
                        Toast.makeText(DateTimeConfigActivity.this, "Date was modified.", Toast.LENGTH_SHORT).show();
                    }
                }, year, month, dayOfMonth);
                dpd.show(getFragmentManager(), "datePicker");
            }
        }
    }

    private Calendar getTime() {
        CacheManager cm = CacheManager.getInstance(this);
        Calendar calendar = Calendar.getInstance();
        if (cm.containsPreference(CacheManager.SERVER_TIME) || !cm.containsPreference(CacheManager.ELAPSED_TIME)) {
            Calendar calSavedServerTime = Calendar.getInstance();
            Calendar calSavedElapsedTime = Calendar.getInstance();
            Calendar calCurrentTime = Calendar.getInstance();
            calSavedServerTime.setTimeInMillis(cm.getLongPreference(CacheManager.SERVER_TIME));
            calSavedElapsedTime.setTimeInMillis(cm.getLongPreference(CacheManager.ELAPSED_TIME));
            long diffTime = calSavedElapsedTime.getTimeInMillis() - SystemClock.elapsedRealtime();
            long currentTime = calSavedServerTime.getTimeInMillis() - diffTime;
            calCurrentTime.setTimeInMillis(currentTime);
            Calendar elapsed = Calendar.getInstance();
            elapsed.setTimeInMillis(SystemClock.elapsedRealtime());
            return calCurrentTime;
        }
        return calendar;
    }

    private Calendar getTimeViewPreferences() {
        CacheManager cm = CacheManager.getInstance(this);
        Calendar calSavedServerTime = Calendar.getInstance();
        Calendar calSavedElapsedTime = Calendar.getInstance();
        Calendar calCurrentTime = Calendar.getInstance();
        calSavedServerTime.setTimeInMillis(cm.getLongPreference(CacheManager.SERVER_TIME));
        calSavedElapsedTime.setTimeInMillis(cm.getLongPreference(CacheManager.ELAPSED_TIME));
        long diffTime = calSavedElapsedTime.getTimeInMillis() - SystemClock.elapsedRealtime();
        long currentTime = calSavedServerTime.getTimeInMillis() - diffTime;
        calCurrentTime.setTimeInMillis(currentTime);

        Calendar elapsed = Calendar.getInstance();
        elapsed.setTimeInMillis(SystemClock.elapsedRealtime());
        return calCurrentTime;
    }
}
