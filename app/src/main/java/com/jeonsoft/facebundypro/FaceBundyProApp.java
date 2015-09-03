package com.jeonsoft.facebundypro;

import android.app.Application;
import android.content.res.Configuration;

import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.jeonsoft.facebundypro.utils.TypefaceUtil;

/**
 * Created by WendellWayne on 7/25/2015.
 */
public class FaceBundyProApp extends Application {
    private static FaceBundyProApp instance;

    public static FaceBundyProApp getInstance() {
        return instance;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        GlobalConstants.getInstance().setContext(this);
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/CaviarDreams.ttf"); // font from assets: "assets/fonts/Roboto-Regular.ttf
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
