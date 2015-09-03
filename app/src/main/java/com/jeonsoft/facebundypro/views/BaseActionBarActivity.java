package com.jeonsoft.facebundypro.views;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.gc.materialdesign.widgets.SnackBar;
import com.jeonsoft.facebundypro.biometrics.licensing.LicensingManager;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.widgets.Crouton;
import com.jeonsoft.facebundypro.widgets.Style;

/**
 * Created by WendellWayne on 2/14/2015.
 */
public abstract class BaseActionBarActivity extends ActionBarActivity {
    public static final String[] LICENSES = {
            LicensingManager.LICENSE_FACE_EXTRACTION,
            LicensingManager.LICENSE_FACE_DETECTION,
            LicensingManager.LICENSE_FACE_MATCHING,
            LicensingManager.LICENSE_DEVICES_CAMERAS};

    public void logError(String message) {
        Logger.logE(message);
    }

    public void showCrouton(final String message, final Style style) {
        final Activity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Crouton.makeText(context, message, style).show();
            }
        });
    }

    public void showInfoDialog() {

    }

    public void logInfo(String message) {
        Logger.logI(message);
    }

    public void logDebug(String message) {
        Logger.logD(message);
    }

    public void showSnackBar(final String message, final String action, final View.OnClickListener listener) {
        final Activity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SnackBar snackbar = new SnackBar(context, message, action, listener);
                snackbar.show();
            }
        });
    }

    public void playRingtone(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playBeep() {
        MediaPlayer m = new MediaPlayer();
        try {
            if (m.isPlaying()) {
                m.stop();
                m.release();
                m = new MediaPlayer();
            }

            AssetFileDescriptor descriptor = getAssets().openFd("fail.wav");
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            m.prepare();
            m.setVolume(1f, 1f);
            m.setLooping(true);
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
