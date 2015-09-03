package com.jeonsoft.facebundypro.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;

import com.jeonsoft.facebundypro.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by WendellWayne on 6/29/2015.
 */
public class FetchAddressIntentService extends IntentService {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.jeonsoft.facebundy";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String GEO_TAGGING_SERVICE = "com.jeonsoft.facebundy.geotagging";

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Intent intent = new Intent(GEO_TAGGING_SERVICE);
        intent.putExtra("RESULT_CODE", resultCode);
        intent.putExtra("MESSAGE", message);
        sendBroadcast(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        double latitude = intent.getDoubleExtra(LATITUDE, -1);
        double longitude = intent.getDoubleExtra(LONGITUDE, -1);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException ex) {
            Logger.logE("Service no available." + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Logger.logE("Invalid latitude or longitude values.");
        } catch(Exception ex) {
            Logger.logE("Error geotagging: " + ex.getMessage());
        }

        if (addresses == null || addresses.size() == 0) {
            Logger.logE("No address found");
            deliverResultToReceiver(FAILURE_RESULT, "An error has occurred.");
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            deliverResultToReceiver(SUCCESS_RESULT, TextUtils.join(System.getProperty("line.separator"), addressFragments));
        }
    }
}
