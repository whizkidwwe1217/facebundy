package com.jeonsoft.facebundypro;

import android.os.Bundle;
import android.view.WindowManager;

import com.jeonsoft.facebundypro.biometrics.face.Model;
import com.jeonsoft.facebundypro.views.BaseActionBarActivity;
import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.view.NFaceView;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.lang.NCore;
import com.neurotec.util.concurrent.CompletionHandler;

import java.util.EnumSet;

/**
 * Created by WendellWayne on 2/15/2015.
 */
public class FaceEnrollmentActivity extends BaseActionBarActivity {
    private NFaceView mFaceView;
    private NSubject mSubject;
    private NBiometricClient mClient;
    private boolean checkDuplicates = true;
    private Status mStatus = Status.CAPTURING;

    private enum Source {
        CAMERA
    }

    private enum Status {
        CAPTURING,
        OPENING_FILE,
        TEMPLATE_CREATED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NCore.setContext(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        setContentView(R.layout.activity_enrol_face);
        mFaceView = (NFaceView) findViewById(R.id.camera_view);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initBiometrics();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //LicensingManager.getInstance().release(Arrays.asList(LICENSES));
    }

    private void initBiometrics() {
        mClient = Model.getInstance().getClient();
        mSubject = Model.getInstance().getSubject();
        mClient.getDeviceManager().setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));
        mClient.initialize();
        NCamera camera = (NCamera) connectDevice(mClient.getDeviceManager(), Source.CAMERA);
        mClient.setFaceCaptureDevice(camera);
        mClient.list(NBiometricOperation.LIST, subjectListHandler);
        startCapturing();
    }

    private CompletionHandler<NSubject[], ? super NBiometricOperation> subjectListHandler = new CompletionHandler<NSubject[], NBiometricOperation>() {
        @Override
        public void completed(NSubject[] nSubjects, NBiometricOperation nBiometricOperation) {
            Model.getInstance().setSubjects(nSubjects);
        }

        @Override
        public void failed(Throwable throwable, NBiometricOperation nBiometricOperation) {

        }
    };

    private NDevice connectDevice(NDeviceManager deviceManager, Source source) {
        switch (source) {
            case CAMERA: {
                // Get count of connected devices.
                int count = deviceManager.getDevices().size();
                if (count == 0) {
                    throw new RuntimeException("No cameras found, exiting!");
                }
                // Select the first available camera.
                if (deviceManager.getDevices().size() > 1)
                    return deviceManager.getDevices().get(1);
                return deviceManager.getDevices().get(0);
            }
            default:
                throw new AssertionError("Not recognised input source");
        }
    }

    private void startCapturing() {
        mStatus = Status.CAPTURING;
        NSubject subject = new NSubject();
        NFace face = new NFace();
        face.setCaptureOptions(EnumSet.of(NBiometricCaptureOption.MANUAL));
        mFaceView.setFace(face);
        mFaceView.setShowEmotions(true);
        mFaceView.setShowGender(true);
        subject.getFaces().add(face);
        capture(subject);
    }

    private void capture(NSubject subject) {
        if (subject == null) throw new NullPointerException("subject");
        mSubject = subject;
        NBiometricTask task = mClient.createTask(EnumSet.of(NBiometricOperation.CREATE_TEMPLATE), subject);
        mClient.performTask(task, NBiometricOperation.CREATE_TEMPLATE, completionHandler);
        onOperationStarted(NBiometricOperation.CAPTURE);
    }

    private CompletionHandler<NBiometricTask, NBiometricOperation> completionHandler = new CompletionHandler<NBiometricTask, NBiometricOperation>() {
        @Override
        public void completed(NBiometricTask nBiometricTask, NBiometricOperation nBiometricOperation) {
            final NBiometricStatus status = nBiometricTask.getStatus();
            onOperationCompleted(nBiometricOperation, nBiometricTask);
        }

        @Override
        public void failed(Throwable throwable, NBiometricOperation nBiometricOperation) {
            logError("Error: " + throwable.getMessage() != null ? throwable.getMessage() : throwable.toString());
        }
    };

    protected void onOperationStarted(NBiometricOperation operation) {
        if (operation == NBiometricOperation.CAPTURE) {
            mStatus = Status.CAPTURING;
        }
    }

    protected void onOperationCompleted(final NBiometricOperation operation, final NBiometricTask task) {
        if (operation == NBiometricOperation.CREATE_TEMPLATE && task.getStatus() == NBiometricStatus.OK) {
            mStatus = Status.TEMPLATE_CREATED;
        }
    }

    protected void stop() {
        mClient.force();
    }

    protected void cancel() {
        if (mClient != null)
            mClient.cancel();
    }
}
