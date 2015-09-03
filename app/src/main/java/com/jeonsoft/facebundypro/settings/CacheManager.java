package com.jeonsoft.facebundypro.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Wendell Wayne on 6/21/2014.
 */
public final class CacheManager {
    public static final String SERVER_TIME = "pref_server_time";
    public static final String ELAPSED_TIME = "pref_elapsed_time";
    public static final String COMPANY_ID = "pref_company_id";
    public static final String EDITION = "pref_edition";
    public static final String DEVICE_NAME = "pref_device_name";
    public static final String ACCESS_CODE_LENGTH = "pref_access_code_length";
    public static final String LICENSE_NO = "pref_license_no";
    public static final String ACTIVATION_KEY = "pref_activation_key";
    public static final String ACTIVATED = "pref_activated";
    public static final String NEUROTEC_ACTIVATED = "pref_neurotec_activated";
    public static final String SERVER_HOSTS = "pref_key_server_host";
    public static final String ADMIN_PASSWORD = "pref_key_admin_pin";
    public static final String PREFERENCE_NAME = "JeonsoftPreferences";
    public static final String ENABLE_FACE_ENROLLMENT = "pref_key_enable_face_enrollment";
    public static final String ENABLE_ITINERARIES = "pref_key_enable_itineraries";
    public static final String ENABLE_REALTIME_UPLOAD = "pref_key_enable_realtime_upload";
    public static final String SHOW_TIME_LOGS = "pref_key_showlogs";
    public static final String MAX_ACCESS_CODE_LENGTH = "pref_key_max_access_code_length";
    public static final String HIDE_DATETIME_WARNING = "pref_hide_datetime_warning";
    public static final String TIME_LAST_SYNC_SOURCE = "pref_time_last_sync_source";

    private static CacheManager instance;
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private CacheManager(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        editor = preferences.edit();
    }

    public boolean containsPreference(String key) {
        return preferences.contains(key);
    }

    public String getStringPreference(String key) {
        return preferences.getString(key, "");
    }

    public int getIntPreference(String key) {
        return preferences.getInt(key, -1);
    }

    public boolean getBooleanPreference(String key) {
        return preferences.getBoolean(key, false);
    }

    public float getFloatPreference(String key) {
        return preferences.getFloat(key, -1F);
    }

    public long getLongPreference(String key) {
        return preferences.getLong(key, 0);
    }

    public void setPreference(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void setPreference(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void setPreference(String key, String[] values) {
        editor.putStringSet(key, new HashSet<String>(Arrays.asList(values)));
        editor.commit();
    }

    public Object[] getArrayPreference(String key) {
        HashSet<String> set = (HashSet<String>) preferences.getStringSet(key, null);
        if (set != null)
            return set.toArray();
        return null;
    }

    public void setPreference(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public void setPreference(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public void setPreference(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void removePreference(String key) {
        editor.remove(key);
        editor.commit();
    }

    public void clearPreference() {
        editor.clear();
        editor.commit();
    }

    public void deletePreference(String key) {
        editor.remove(key);
        editor.commit();
    }

    public static CacheManager getInstance(Context context) {
        if (instance == null)
            instance = new CacheManager(context);
        return instance;
    }
}
