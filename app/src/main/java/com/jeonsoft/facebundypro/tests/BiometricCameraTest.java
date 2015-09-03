package com.jeonsoft.facebundypro.tests;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.biometrics.licensing.ActivationCallback;
import com.jeonsoft.facebundypro.biometrics.licensing.LicenseActivator;
import com.jeonsoft.facebundypro.biometrics.licensing.LicensingManager;
import com.jeonsoft.facebundypro.biometrics.licensing.LicensingState;
import com.jeonsoft.facebundypro.data.SubjectsDataSource;
import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.jeonsoft.facebundypro.views.BaseActionBarActivity;
import com.jeonsoft.facebundypro.widgets.Style;
import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.view.NFaceView;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.lang.NCore;
import com.neurotec.plugins.NPlugin;
import com.neurotec.util.concurrent.CompletionHandler;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by WendellWayne on 6/5/2015.
 */
public class BiometricCameraTest extends BaseActionBarActivity {
    private NBiometricClient biometricClient;
    private NFaceView cameraControl;
    private LicensingState licensingState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_bundy_camera);
        NCore.setContext(this);
        LicensingManager.getInstance().obtain(BiometricCameraTest.this, new LicensingManager.LicensingStateCallback() {
            @Override
            public void onLicensingStateChanged(LicensingState state) {
                if (state == LicensingState.OBTAINED) {
                    showCrouton(state.toString(), Style.INFO);
                    initControls();
                } else if (state == LicensingState.NOT_OBTAINED) {
                    showCrouton(state.toString(), Style.ALERT);
                } else {

                }
            }
        }, Arrays.asList(LICENSES));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LicensingManager.getInstance().release(Arrays.asList(LICENSES));
    }

    private void openDatabase() {
        try {
            SubjectsDataSource.getInstance(this).open();
        } catch (Exception ex) {
            //showCrouton(ex.getMessage(), Style.ALERT);
            showSnackBar(ex.getMessage(), "", null);
        }
    }

    private FloatingActionButton btnTimeIn;

    private void initControls() {
        btnTimeIn = (FloatingActionButton) findViewById(R.id.fabIn);
        btnTimeIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIn();
            }
        });

        cameraControl = (NFaceView) findViewById(R.id.camera_view);

        biometricClient = new NBiometricClient();
        //openDatabase();
        GlobalConstants.getInstance().setContext(this);
        //biometricClient.setDatabaseConnectionToSQLite(GlobalConstants.getInstance().getContext().getDatabasePath(GlobalConstants.DATABASE_NAME).getAbsolutePath());
        biometricClient.setUseDeviceManager(true);
        for (NPlugin plugin : NDeviceManager.getPluginManager().getPlugins()) {
            logError(String.format("Plugin name => %s, Error => %s", plugin.getModule().getName(), plugin.getError()));
        }
        for (NDevice device : biometricClient.getDeviceManager().getDevices()) {
            logError(String.format("Device name => %s", device.getDisplayName()));
        }

        NDeviceManager deviceManager = biometricClient.getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));
        biometricClient.initialize();

        startCapturing();
    }

    private void timeIn() {

    }

    private void startCapturing() {
        NSubject subject = new NSubject();
        NFace face = new NFace();
        face.setCaptureOptions(EnumSet.of(NBiometricCaptureOption.MANUAL));
        cameraControl.setFace(face);
        subject.getFaces().add(face);

        NCamera camera = (NCamera) connectDevice(biometricClient.getDeviceManager());
        biometricClient.setFaceCaptureDevice(camera);
        biometricClient.capture(subject, subject, completionHandler);
    }

    private NDevice connectDevice(NDeviceManager deviceManager) {
        int count = deviceManager.getDevices().size();
        if (count == 0) {
            throw new RuntimeException("No cameras found, exiting!");
        }
        // Select the first available camera.
        if (deviceManager.getDevices().size() > 1)
            return deviceManager.getDevices().get(1);
        return deviceManager.getDevices().get(0);
    }

    private void stopCapturing() {
        biometricClient.force();
    }

    private CompletionHandler<NBiometricStatus, NSubject> completionHandler = new CompletionHandler<NBiometricStatus, NSubject>() {
        @Override
        public void completed(NBiometricStatus result, NSubject subject) {
            if (result == NBiometricStatus.OK) {
                NFace face = subject.getFaces().get(0);
                if (face != null) {
                    showCrouton("Face detected.", Style.CONFIRM);
                } else {
                    showCrouton("Face not detected.", Style.ALERT);
                }
            } else {
                showCrouton("Face enrollment: " + result.toString(), Style.ALERT);
            }
        }

        @Override
        public void failed(Throwable throwable, NSubject subject) {

        }
    };
}
