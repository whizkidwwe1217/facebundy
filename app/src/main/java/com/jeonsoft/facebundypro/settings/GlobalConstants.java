package com.jeonsoft.facebundypro.settings;

import android.content.Context;
import android.os.Environment;

import com.jeonsoft.facebundypro.licensing.AppEditions;
import com.jeonsoft.facebundypro.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by WendellWayne on 3/12/2015.
 */
public final class GlobalConstants {
    public static final String MANUAL_SYNC_SOURCE = "Manual";
    public static final String NTP_SYNC_SOURCE = "NTP Server";
    public static final String GPS_SYNC_SOURCE = "GPS";
    public static final String UNKNOWN_SYNC_SOURCE = "Unknown";

    public static final String FACEBUNDY_SERVER_URL = "http://activation.facebundy.com:9001"; //"http://activation.facebundy.com";
    public static final String FACEBUNDY_LICENSE_ACTIVATION_URL_ACTION = "/license/activate";
    public static final String FACEBUNDY_AUTHENTICATION_TOKEN_URL = "/license/get_authenticity_token";
    public static final String DATABASE_NAME = "FaceTimeLogs.db";
    private static GlobalConstants instance;
    private Context context;

    private GlobalConstants() {

    }

    public AppEditions getAppEdition() {
        File file = new File(getCacheDir(context) + "/switch.dat");
        if (file.exists()) {
            FileReader fr;
            try {
                fr = new FileReader(file);
                BufferedReader bf = new BufferedReader(fr);
                int edition = Integer.parseInt(bf.readLine().trim());
                return AppEditions.fromInt(edition);
            } catch(Exception ex) {
                Logger.logD("Error reading switcher. " + ex.getMessage());
                return AppEditions.fromInt(CacheManager.getInstance(context).getIntPreference(CacheManager.EDITION));
            }
        }
        return AppEditions.fromInt(CacheManager.getInstance(context).getIntPreference(CacheManager.EDITION));
    }

    public static GlobalConstants getInstance() {
        if (instance == null)
            instance = new GlobalConstants();
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
    public static final String getFacebundyLicenseActivationUrl() {
        return FACEBUNDY_SERVER_URL.concat(FACEBUNDY_LICENSE_ACTIVATION_URL_ACTION);
    }

    public static final String getFacebundyAuthenticationTokenUrl() {
        return FACEBUNDY_SERVER_URL.concat(FACEBUNDY_AUTHENTICATION_TOKEN_URL);
    }

    public static String getCacheDir(final Context context) {
        String sdState = android.os.Environment.getExternalStorageState();
        File cacheDir;
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            File sdDir = android.os.Environment.getExternalStorageDirectory();
            cacheDir = new File(sdDir, "data/jeonsoft");
        } else {
            cacheDir = context.getCacheDir();
        }

        if (!cacheDir.exists())
            cacheDir.mkdirs();
        return cacheDir.getAbsolutePath();
    }

    public static String getThumbnailCacheDir(final Context context) {
        String cacheDir = getCacheDir(context);
        File thumbnails = new File(cacheDir + "/thumbnails");
        if (!thumbnails.exists())
            thumbnails.mkdirs();
        return thumbnails.getAbsolutePath();
    }

    public static String getEmployeeFaceTemplatesDir(final Context context) {
        String cacheDir = getCacheDir(context);
        File faceTemplateDir = new File(cacheDir + "/face_templates");
        if (!faceTemplateDir.exists())
            faceTemplateDir.mkdirs();
        return faceTemplateDir.getAbsolutePath();
    }

    public static String getEmployeePhotosDirectory(final Context context) {
        String cacheDir = getCacheDir(context);
        File imageDir = new File(cacheDir + "/employee_photos");
        if (!imageDir.exists())
            imageDir.mkdirs();
        return imageDir.getAbsolutePath();
    }
}
