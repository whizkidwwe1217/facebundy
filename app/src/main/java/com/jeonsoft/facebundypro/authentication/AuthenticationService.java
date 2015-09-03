package com.jeonsoft.facebundypro.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by WendellWayne on 2/14/2015.
 */
public class AuthenticationService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new AccountAuthenticator(this).getIBinder();
    }
}
