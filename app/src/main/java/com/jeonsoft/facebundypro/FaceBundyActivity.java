package com.jeonsoft.facebundypro;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ProgressBarIndeterminate;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.jeonsoft.facebundypro.biometrics.licensing.ActivationCallback;
import com.jeonsoft.facebundypro.biometrics.licensing.LicenseActivator;
import com.jeonsoft.facebundypro.biometrics.licensing.LicensingManager;
import com.jeonsoft.facebundypro.biometrics.licensing.LicensingState;
import com.jeonsoft.facebundypro.biometrics.face.Model;
import com.jeonsoft.facebundypro.data.Employee;
import com.jeonsoft.facebundypro.data.EmployeeDataSource;
import com.jeonsoft.facebundypro.data.ItineraryProject;
import com.jeonsoft.facebundypro.data.ItineraryProjectDataSource;
import com.jeonsoft.facebundypro.data.Subject;
import com.jeonsoft.facebundypro.data.SubjectsDataSource;
import com.jeonsoft.facebundypro.data.TimelogDataSource;
import com.jeonsoft.facebundypro.data.TimeLog;
import com.jeonsoft.facebundypro.licensing.AppEditions;
import com.jeonsoft.facebundypro.location.GPSTracker;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.ConnectivityHelper;
import com.jeonsoft.facebundypro.net.ReachableServerHost;
import com.jeonsoft.facebundypro.net.ReachableServerHostListener;
import com.jeonsoft.facebundypro.net.ServerHostStatus;
import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.jeonsoft.facebundypro.utils.BatteryLevelReceiver;
import com.jeonsoft.facebundypro.utils.DialogUtils;
import com.jeonsoft.facebundypro.utils.FileUtils;
import com.jeonsoft.facebundypro.utils.NImageUtils;
import com.jeonsoft.facebundypro.utils.ObjectCallback;
import com.jeonsoft.facebundypro.utils.TimeUtils;
import com.jeonsoft.facebundypro.views.BaseActionBarActivity;
import com.jeonsoft.facebundypro.views.CircleButton;
import com.jeonsoft.facebundypro.views.ItineraryProjectItemActivity;
import com.jeonsoft.facebundypro.views.ItineraryProjectListActivity;
import com.jeonsoft.facebundypro.views.LogsReportActivity;
import com.jeonsoft.facebundypro.views.PinCodeDialogFragment;
import com.jeonsoft.facebundypro.views.SubjectManagerService;
import com.jeonsoft.facebundypro.widgets.DelayAutoCompleteTextView;
import com.jeonsoft.facebundypro.widgets.EmployeeAdapterItem;
import com.jeonsoft.facebundypro.widgets.EmployeeAutoCompleteAdapter;
import com.jeonsoft.facebundypro.widgets.Style;
import com.neurotec.biometrics.NBiometric;
import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NBiometricType;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NLMatchingDetails;
import com.neurotec.biometrics.NMatchingDetails;
import com.neurotec.biometrics.NMatchingResult;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.view.NFaceView;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.images.NImage;
import com.neurotec.images.NImageFormat;
import com.neurotec.images.NImageRotateFlipType;
import com.neurotec.io.NBuffer;
import com.neurotec.io.NFile;
import com.neurotec.lang.NCore;
import com.neurotec.plugins.NPlugin;
import com.neurotec.util.concurrent.CompletionHandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class FaceBundyActivity extends BaseActionBarActivity
        implements LicensingManager.LicensingStateCallback, ClockTypeListener {
    private enum Source {
        CAMERA
    }

    private enum Status {
        CAPTURING,
        OPENING_FILE,
        TEMPLATE_CREATED;

        public static Status fromInt(int x) {
            switch (x) {
                case 1:
                    return TEMPLATE_CREATED;
                case 2:
                    return OPENING_FILE;
                default:
                    return CAPTURING;
            }
        }

        public static int toInt(Status status) {
            if (status == TEMPLATE_CREATED)
                return 1;
            else if (status == OPENING_FILE)
                return 2;
            else
                return 0;
        }
    }

    private DelayAutoCompleteTextView edtAccessCode;
    private LinearLayout llPreview;
    private TextView tvEmployeeName, tvNetworkTime;
    private CircleImageView imgPreview;

    private FloatingActionButton btnTimeIn, btnTimeOut, btnFloatEnroll, btnVerify;
    private FloatingActionsMenu btnFloatCapture;
    private NSubject mSubject;
    private NFaceView mFaceView;
    private NBiometricClient mClient;
    private Source mSource = Source.CAMERA;
    private boolean checkDuplicates = false;
    private ProgressDialog progressDialog;
    private boolean mLicensesObtained = false;
    private Status mStatus = Status.CAPTURING;
    private boolean mAppClosing;
    private String clockType = "";
    public static final int LICENSE_ACTIVATION_REQUEST_CODE = 0x000;
    public static final int LICENSE_ACTIVATED_RESULT_CODE = 0x001;
    private boolean faceCaptured = false;
    private boolean isEnrollmentMode = false;
    private double latitude = -1, longitude = -1;
    private TextView tvAppDate, tvAppTime, tvBatteryDesc, tvLastSync;
    private ButtonFlat btnDateTimeSync, btnDateTimeDismiss;
    private RelativeLayout rlDateTime, rlBatteryStatus;
    private LinearLayout llapp_date_time_container;
    private ImageView imgDateTimeWarningCompact;
    private ProgressBarIndeterminate progress;

    private void initStyles() {
        /*if (Build.VERSION.SDK_INT >= 19) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }*/
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStyles();
        openDatabase();

        if (savedInstanceState != null) {
            mStatus = Status.fromInt(savedInstanceState.getInt(STATE_CAMERA_STATUS));
            mLicensesObtained = savedInstanceState.getBoolean(STATE_LICENSE_OBTAINED);
        }

        int numCameras = Camera.getNumberOfCameras();
        if (numCameras <= 0) {
            showSnackBar("No camera device found.", "OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.exit(0);
                }
            });
            System.exit(0);
        }

        CacheManager cm = CacheManager.getInstance(this);

        if (cm.containsPreference(CacheManager.ACTIVATED) && cm.getBooleanPreference(CacheManager.ACTIVATED)) {
            String activationKey = cm.getStringPreference(CacheManager.ACTIVATION_KEY);
            int licenseNo = cm.getIntPreference(CacheManager.LICENSE_NO);
            String deviceName = cm.getStringPreference(ActivationActivity.DEVICE_NAME);
            int edition = cm.getIntPreference(CacheManager.EDITION);
            int companyId = cm.getIntPreference(CacheManager.COMPANY_ID);
            int accessCodeLength = Integer.parseInt(cm.getStringPreference(CacheManager.MAX_ACCESS_CODE_LENGTH));
            cm.setPreference(CacheManager.MAX_ACCESS_CODE_LENGTH, String.valueOf(accessCodeLength));
            validateAppLicense(deviceName, licenseNo, activationKey, AppEditions.fromInt(edition), companyId, accessCodeLength);
        } else
            initAppLicense();
        setGpsTime(false);
    }

    private void openDatabase() {
        try {
            SubjectsDataSource.getInstance(this).open();
        } catch (Exception ex) {
            //showCrouton(ex.getMessage(), Style.ALERT);
            showSnackBar(ex.getMessage(), "", null);
        }
    }

    private void closeDatabase() {
        try {
            SubjectsDataSource.getInstance(this).close();
        } catch (Exception ex) {
            //showCrouton(ex.getMessage(), Style.ALERT);
            showSnackBar(ex.getMessage(), "", null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timerTask != null)
            timerTask.cancel();

        TimeLogUploadHelper helper = TimeLogUploadHelper.getInstance(this);
        try {
            helper.unregister();
        } catch(Exception ex) {}

        try {
            unregisterReceiver(subjectManagerReceiver);
        } catch(Exception ex) {}

        unregisterReceiver(mBatteryStatusReceiver);
    }

    public static final String STATE_LICENSE_OBTAINED = "LICENSE_OBTAINED";
    public static final String STATE_CAMERA_STATUS = "STATUS";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_LICENSE_OBTAINED, mLicensesObtained);
        outState.putInt(STATE_CAMERA_STATUS, Status.toInt(mStatus));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mStatus = Status.fromInt(savedInstanceState.getInt(STATE_CAMERA_STATUS));
        mLicensesObtained = savedInstanceState.getBoolean(STATE_LICENSE_OBTAINED);
    }

    private final BroadcastReceiver subjectManagerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String id = intent.getStringExtra("SUBJECT_TO_DELETE");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logError("Deleted: " + id);
                    delete(id);
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setClock();

        monitorBatteryStatus();

        CacheManager cm = CacheManager.getInstance(this);
        if (cm.containsPreference(CacheManager.SERVER_TIME) && cm.containsPreference(CacheManager.ELAPSED_TIME)) {
            long elapsed = cm.getLongPreference(CacheManager.ELAPSED_TIME);
            long realtime = SystemClock.elapsedRealtime();
            long difftime = realtime - elapsed;
            if (difftime < 0) {
                showSnackBar("FaceBundy date & time is out of sync!", "Sync", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getCurrentTime(true);
                    }
                });
            }
        }

        boolean enableEnrollment = CacheManager.getInstance(this).getBooleanPreference(CacheManager.ENABLE_FACE_ENROLLMENT);
        if (btnFloatEnroll != null)
            btnFloatEnroll.setVisibility(enableEnrollment && getAppEdition() == AppEditions.ExtractionAndMatching ? View.VISIBLE : View.GONE);

        TimeLogUploadHelper helper = TimeLogUploadHelper.getInstance(this);
        try {
            helper.register();
        } catch(Exception ex) {}

        try {
            registerReceiver(subjectManagerReceiver, new IntentFilter(SubjectManagerService.SUBJECT_MANAGER_SEVICE));
        } catch(Exception ex) {}

        if (mLicensesObtained && mStatus == Status.CAPTURING)
            startCapturing();
    }

    private void reloadFaceTemplates() {
        mClient = new NBiometricClient();
        mClient.setDatabaseConnectionToSQLite(GlobalConstants.getInstance().getContext().getDatabasePath(GlobalConstants.DATABASE_NAME).getAbsolutePath());
        mClient.setUseDeviceManager(true);
        mSubject = new NSubject();
        for (NPlugin plugin : NDeviceManager.getPluginManager().getPlugins()) {
            Log.i("Model", String.format("Plugin name => %s, Error => %s", plugin.getModule().getName(), plugin.getError()));
        }
        for (NDevice device : mClient.getDeviceManager().getDevices()) {
            Log.i("Device", String.format("Device name => %s", device.getDisplayName()));
        }

        mClient.getDeviceManager().setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));

        /*@TODO: SETTINGS FOR BIOMETRICS */
        String thres = CacheManager.getInstance(this).getStringPreference("pref_key_matching_threshold");
        if (thres.equals(""))
            thres = "24";
        int threshold = Integer.parseInt(thres);

        mClient.setMatchingThreshold(threshold);
        //mClient.setFacesCreateThumbnailImage(true);
        //mClient.setFacesThumbnailImageWidth(90);

        try {
            mClient.initialize();
            NCamera camera = (NCamera) connectDevice(mClient.getDeviceManager(), Source.CAMERA);
            mClient.setFaceCaptureDevice(camera);
            mAppClosing = false;

            mClient.list(NBiometricOperation.LIST, subjectListHandler);
        } catch(Exception ex) {}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FaceBundyActivity.LICENSE_ACTIVATION_REQUEST_CODE) {
            if (resultCode == FaceBundyActivity.LICENSE_ACTIVATED_RESULT_CODE) {
                String activationKey = data.getStringExtra(ActivationActivity.ACTIVATION_KEY);
                int licenseNo = data.getIntExtra(ActivationActivity.LICENSE_NO, 0);
                int accessCodeLength = data.getIntExtra(ActivationActivity.ACCESS_CODE_LENGTH, 0);
                String deviceName = data.getStringExtra(ActivationActivity.DEVICE_NAME);
                int edition = data.getIntExtra(ActivationActivity.EDITION, 0);
                int companyId = data.getIntExtra(ActivationActivity.COMPANY_ID, 0);
                validateAppLicense(deviceName, licenseNo, activationKey, AppEditions.fromInt(edition), companyId, accessCodeLength);
            } else {
                finish();
            }
        } else if (requestCode == SETTINGS_REQUEST) {
            setMaxLength(edtAccessCode, getAccessCodeLength());
        }
    }

    private void validateAppLicense(String deviceName, int licenseNo, String key, AppEditions edition, int companyId, int accessCodeLength) {
        CacheManager cm = CacheManager.getInstance(this);
        cm.setPreference(CacheManager.ACTIVATION_KEY, key);
        cm.setPreference(CacheManager.LICENSE_NO, licenseNo);
        cm.setPreference(CacheManager.DEVICE_NAME, deviceName);
        cm.setPreference(CacheManager.EDITION, AppEditions.toInt(edition));
        cm.setPreference(CacheManager.COMPANY_ID, companyId);
        cm.setPreference(CacheManager.MAX_ACCESS_CODE_LENGTH, String.valueOf(accessCodeLength));
        cm.setPreference(CacheManager.ACTIVATED, true);

        if (getAppEdition() != AppEditions.Basic) {
            NCore.setContext(FaceBundyActivity.this);
            verifyBiometricLicense();
        }
    }

    private int getAccessCodeLength() {
        int length = 4;
        int len = Integer.parseInt(CacheManager.getInstance(FaceBundyActivity.this).getStringPreference(CacheManager.MAX_ACCESS_CODE_LENGTH));
        if (len > 0)
            length = len;
        return length;
    }

    public void setMaxLength(EditText edt, int length) {
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(length);
        edt.setFilters(FilterArray);
    }

    private void initAppLicense() {
        Intent intent = new Intent(this, ActivationActivity.class);
        startActivityForResult(intent, FaceBundyActivity.LICENSE_ACTIVATION_REQUEST_CODE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDatabase();
        if (gpsTracker != null)
            gpsTracker.stopGPS();

        if (getAppEdition() != AppEditions.Basic)
            LicensingManager.getInstance().release(Arrays.asList(LICENSES));
    }

    private void verifyBiometricLicense() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!CacheManager.getInstance(FaceBundyActivity.this).getBooleanPreference(CacheManager.NEUROTEC_ACTIVATED)) {
                    Logger.logE("Activating license from neurotec server...");
                    LicenseActivator a = new LicenseActivator(FaceBundyActivity.this);
                    a.setActivationCallback(new ActivationCallback() {
                        @Override
                        public void onActivate(boolean activated) {
                            CacheManager cm = CacheManager.getInstance(FaceBundyActivity.this);
                            cm.setPreference(CacheManager.NEUROTEC_ACTIVATED, activated);
                        }

                        @Override
                        public void onDeactivate(boolean deactivated) {
                            CacheManager cm = CacheManager.getInstance(FaceBundyActivity.this);
                            cm.setPreference(CacheManager.NEUROTEC_ACTIVATED, !deactivated);
                        }
                    });
                    a.activate();
                }
                LicensingManager.getInstance().obtain(FaceBundyActivity.this, FaceBundyActivity.this, Arrays.asList(LICENSES));
            }
        });
    }

    private void switchCamera() {
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(FaceBundyActivity.this, R.anim.anim_switch_camera);
        mFaceView.startAnimation(hyperspaceJumpAnimation);
        hyperspaceJumpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onSwitchCamera();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private boolean isVerifyTest = false;

    private void initComponents() {
        setContentView(R.layout.activity_face_bundy_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        llPreview = (LinearLayout) findViewById(R.id.llPreview);
        imgPreview = (CircleImageView) findViewById(R.id.imgImagePreview);
        tvEmployeeName = (TextView) findViewById(R.id.tvEmployeeName);
        tvNetworkTime = (TextView) findViewById(R.id.tvNetworkTime);

        tvAppDate = (TextView) findViewById(R.id.tv_app_date);
        tvAppTime = (TextView) findViewById(R.id.tv_app_time);
        btnDateTimeSync = (ButtonFlat) findViewById(R.id.btnDateTimeSync);
        btnDateTimeSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectivityHelper.isConnected(FaceBundyActivity.this)) {
                    getCurrentTime(true);
                } else {
                    showSnackBar("Can't sync this time. Please connect to the internet.", "Use GPS", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setGpsTime(true);
                        }
                    });
                }
            }
        });
        btnDateTimeDismiss = (ButtonFlat) findViewById(R.id.btnDateTimeDismiss);
        llapp_date_time_container = (LinearLayout) findViewById(R.id.app_date_time_container);
        imgDateTimeWarningCompact = (ImageView) findViewById(R.id.imgDateTimeWarningCompact);
        progress = (ProgressBarIndeterminate) findViewById(R.id.progress);

        tvLastSync = (TextView) findViewById(R.id.tvLastSync);

        imgDateTimeWarningCompact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDateTimeWarning(View.VISIBLE);
                CacheManager.getInstance(FaceBundyActivity.this).setPreference(CacheManager.HIDE_DATETIME_WARNING, false);
            }
        });
        llapp_date_time_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imgDateTimeWarningCompact.getVisibility() == View.VISIBLE) {
                    toggleDateTimeWarning(View.VISIBLE);
                    CacheManager.getInstance(FaceBundyActivity.this).setPreference(CacheManager.HIDE_DATETIME_WARNING, false);
                }
            }
        });
        btnDateTimeDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDateTimeWarning(View.GONE);
                CacheManager.getInstance(FaceBundyActivity.this).setPreference(CacheManager.HIDE_DATETIME_WARNING, true);
            }
        });
        rlDateTime = (RelativeLayout) findViewById(R.id.rlDateTimeWarning);
        rlBatteryStatus = (RelativeLayout) findViewById(R.id.battery_status);
        tvBatteryDesc = (TextView) findViewById(R.id.tvBatteryDesc);

        setClock();

        edtAccessCode = (DelayAutoCompleteTextView) findViewById(R.id.edtAccessCode);
        edtAccessCode.requestFocus();
        edtAccessCode.addTextChangedListener(accessCodeTextWatcher);
        setMaxLength(edtAccessCode, getAccessCodeLength());
        btnTimeIn = (FloatingActionButton) findViewById(R.id.fabIn);
        btnTimeIn.setOnClickListener(btnTimeInListener);
        btnTimeOut = (FloatingActionButton) findViewById(R.id.fabOut);
        btnTimeOut.setOnClickListener(btnTimeOutListener);
        btnVerify = (FloatingActionButton) findViewById(R.id.fabVerify);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVerifyTest = true;
                verify(edtAccessCode.getText().toString().trim());
            }
        });

        btnFloatCapture = (FloatingActionsMenu) findViewById(R.id.famCapture);
        btnFloatCapture.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                hideKeyboard();
                faceCaptured = true;
                btnFloatCapture.setIcon(R.drawable.ic_action_back);
                onStartCapturing();
            }

            @Override
            public void onMenuCollapsed() {
                faceCaptured = false;
                btnFloatCapture.setIcon(R.drawable.ic_action_camera);
                startCapturing();
            }
        });

        btnFloatEnroll = (FloatingActionButton) findViewById(R.id.fabEnroll);
        boolean enableEnrollment = CacheManager.getInstance(this).getBooleanPreference(CacheManager.ENABLE_FACE_ENROLLMENT);
        btnFloatEnroll.setVisibility(enableEnrollment && getAppEdition() == AppEditions.ExtractionAndMatching ? View.VISIBLE : View.GONE);

        btnFloatEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enrollment();

            }
        });

        mFaceView = (NFaceView) findViewById(R.id.camera_view);
        mFaceView.setFaceRectangleColor(getResources().getColor(R.color.accent_color));
        mFaceView.setSoundEffectsEnabled(true);
        initBiometrics(true);
    }

    private void hidePreview() {
        Animation fadeOutAnim = AnimationUtils.loadAnimation(FaceBundyActivity.this, R.anim.anim_fade_out);
        fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llPreview.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        llPreview.startAnimation(fadeOutAnim);
    }

    private void setCaptured(final boolean captured) {
        faceCaptured = captured;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnFloatCapture.collapse();
            }
        });
    }

    private TextWatcher accessCodeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() == getAccessCodeLength()) {
                if (getAppEdition() != AppEditions.Basic) {
                    //btnFloatCapture.setVisibility(faceCaptured ? View.GONE : View.VISIBLE);
                    //buttonContainer.setVisibility(faceCaptured ? View.VISIBLE : View.GONE);
                }
                hideKeyboard();
                Animation fadeOutAnim = AnimationUtils.loadAnimation(FaceBundyActivity.this, R.anim.anim_fade_out);
                fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        llPreview.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                llPreview.startAnimation(fadeOutAnim);
            } else {
                if (getAppEdition() != AppEditions.Basic) {
                    //btnFloatCapture.setVisibility(faceCaptured ? View.GONE : View.VISIBLE);
                    //buttonContainer.setVisibility(faceCaptured ? View.VISIBLE : View.GONE);
                }
            }
        }
    };

    private View.OnClickListener btnTimeInListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isVerifyTest = false;
            hideKeyboard();
            long elapsed = CacheManager.getInstance(FaceBundyActivity.this).getLongPreference(CacheManager.ELAPSED_TIME);
            long realtime = SystemClock.elapsedRealtime();
            long difftime = realtime - elapsed;
            if (difftime < 0) {
                DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.TimeOutOfSync, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(FaceBundyActivity.this, "Requesting time from remote server...", Toast.LENGTH_LONG).show();
                        getCurrentTime(true).getTime();
                    }
                });
                return;
            }
            final String accessCode = edtAccessCode.getText().toString().trim();
            final Date logTime = getCurrentTime(false).getTime();

            onClockIn("IN");
            if (accessCode.length() == 0) {
                DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.UnspecifiedAccessCode);
            } else {
                if (getAppEdition() == AppEditions.ExtractionAndMatching) {
                    verify(accessCode);
                }
                else
                    extractAndClockIn(accessCode, logTime);
            }
        }
    };

    private View.OnClickListener btnTimeOutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isVerifyTest = false;
            hideKeyboard();
            long elapsed = CacheManager.getInstance(FaceBundyActivity.this).getLongPreference(CacheManager.ELAPSED_TIME);
            long realtime = SystemClock.elapsedRealtime();
            long difftime = realtime - elapsed;
            if (difftime < 0) {
                DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.TimeOutOfSync, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getCurrentTime(true).getTime();
                    }
                });
                return;
            }

            final String accessCode = edtAccessCode.getText().toString().trim();
            final Date logTime = getCurrentTime(false).getTime();
            onClockIn("OUT");
            if (accessCode.length() == 0) {
                DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.UnspecifiedAccessCode);
            } else {
                if (getAppEdition() == AppEditions.ExtractionAndMatching) {
                    verify(accessCode);
                }
                else
                    extractAndClockIn(accessCode, logTime);
            }
        }
    };

    private boolean isEnableItinerary() {
        return CacheManager.getInstance(FaceBundyActivity.this).getBooleanPreference(CacheManager.ENABLE_ITINERARIES);
    }

    private void extractAndClockIn(final String accessCode, final Date logTime) {
        final DateFormat df = new SimpleDateFormat("MM/dd/yyyy\nhh:mm:ss a");
        final String strTime = String.valueOf(df.format(logTime));
        final DateFormat dfPersist = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        final String strPersistTime = String.valueOf(dfPersist.format(logTime));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSubject != null) {
                    final NFace face = mSubject.getFaces().get(0);
                    /*final File photo = new File(GlobalConstants.getEmployeePhotosDirectory(FaceBundyActivity.this), accessCode.trim().replace("/", "").replace(":", "").replace(" ", "")
                            + strTime.trim().replace("/", "").replace(":", "").replace(" ", "") + ".jpg");*/
                    final File photo = new File(GlobalConstants.getEmployeePhotosDirectory(FaceBundyActivity.this), accessCode.trim().replace("/", "").replace(":", "").replace(" ", "")
                            + strPersistTime.trim().replace("/", "").replace(":", "").replace(" ", "").concat("_" + String.valueOf(latitude)).concat("_" + String.valueOf(longitude)) + ".jpg");
                    try {
                        final NImage nImage = face.getImage().rotateFlip(NImageRotateFlipType.ROTATE_90_FLIP_XY);

                        new CheckLastLogEntry(accessCode, new ObjectCallback<String, TimeLog>() {
                            @Override
                            public TimeLog onObjectRequestProcess(String... params) {
                                return null;
                            }

                            @Override
                            public void onObjectRequestComplete(TimeLog result) {
                                if (result != null && result.getType().equals(clockType)) {
                                    final TimeLog res = result;
                                    showDoubleLogDialog(res, strPersistTime, photo.getPath(), nImage, strTime, clockType);
                                } else {
                                    try {
                                        nImage.save(photo.getAbsolutePath());
                                        persistLog(accessCode, photo.getPath(), strPersistTime, strTime, clockType);
                                        if (isEnableItinerary()) {
                                            logItinerary(accessCode, strPersistTime, clockType);
                                        }
                                        logDebug("Image Saved: " + photo.getPath());
                                    } catch(Exception e) {
                                        logError(e.getMessage());
                                    }
                                }
                            }

                            @Override
                            public void onObjectRequestError(String message) {

                            }

                            @Override
                            public void onRequestCancelled() {

                            }

                            @Override
                            public void onPreRequest() {

                            }
                        }).execute();
                    } catch (Exception ex) {
                        logError(ex.getMessage());
                    }
                }
            }
        });
    }

    private void logItinerary(final String accessCode, final String strTime, final String type) {
        if (type.equals("IN"))
            addNewItinerary(accessCode, strTime);
        else
            updateItinerary(accessCode, strTime);
    }

    private void addNewItinerary(final String accessCode, final String strTime) {
        DialogUtils.showQuestionDialog(this, "Add Time Log to Itinerary", "Do you want to add this time log to your itinerary?",
                R.drawable.log_itinerary, DialogUtils.DialogImageSize.OneByOne,
                "Not now", "Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(FaceBundyActivity.this, ItineraryProjectItemActivity.class);
                        intent.putExtra("ACCESS_CODE", accessCode);
                        intent.putExtra("TIME_IN", strTime);
                        intent.putExtra("TIME_OUT", "null");
                        intent.putExtra("LATITUDE", latitude);
                        intent.putExtra("LONGITUDE", longitude);
                        intent.putExtra("LOCATION", location);

                        startActivityForResult(intent, ItineraryProjectListActivity.REQUEST_CODE_NEW);
                    }
                });
    }

    private String location;

    class GeoCodeAsync extends AsyncTask<Location, Void, List<Address>> {
        @Override
        protected List<Address> doInBackground(Location... params) {
            try {
                Geocoder geocoder = new Geocoder(getBaseContext());
                return geocoder.getFromLocation(latitude, longitude, 10);
            } catch (Exception ex) {
                Logger.logE(ex.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            super.onPostExecute(addresses);
            if (addresses != null) {
                StringBuilder sb = new StringBuilder();
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
                        sb.append(address.getAddressLine(i)).append(",");
                }
                location = sb.toString();
            }
        }
    }

    private AlertDialog alertDialog = null;
    private void updateItinerary(final String accessCode, final String strTime) {
        DialogUtils.showQuestionDialog(this, "Update Itinerary", "Do you want to clock out your existing itinerary?",
                R.drawable.edit_itinerary, DialogUtils.DialogImageSize.OneByOne,
                "Not now", "Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FaceBundyActivity.this);

                        builder.create();
                        builder.setTitle("Choose an itinerary to clock out.");
                        View view = View.inflate(FaceBundyActivity.this, R.layout.layout_itinerary_uncompleted_list, null);

                        ItineraryProjectDataSource ds = ItineraryProjectDataSource.getInstance(FaceBundyActivity.this);
                        final ItineraryAdapter mAdapter;
                        ArrayList<ItineraryProject> mProjects;
                        try {
                            ds.open();
                            mProjects = ds.getUnCompletedTasks(accessCode);
                            mAdapter = new ItineraryAdapter(FaceBundyActivity.this, R.layout.layout_itinerary_uncompleted, mProjects);

                            ListView listView = (ListView) view.findViewById(R.id.lvItinerary);
                            listView.setAdapter(mAdapter);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    ItineraryProject proj = mAdapter.getItem(position);
                                    ItineraryProjectDataSource dst = ItineraryProjectDataSource.getInstance(FaceBundyActivity.this);
                                    try {
                                        dst.open();
                                        dst.beginTransaction();
                                        dst.updateItinerary(proj.accessCode, proj.project, proj.timeIn, strTime, proj.latitude, proj.longitude, proj.location, proj.id);
                                        dst.setTransactionSuccessful();
                                    } catch(Exception ex) {
                                        Logger.logE(ex.getMessage());
                                    } finally {
                                        if (dst != null && dst.isOpen()) {
                                            dst.endTransaction();
                                            dst.close();
                                        }
                                        if (alertDialog != null && alertDialog.isShowing())
                                            alertDialog.dismiss();
                                    }
                                }
                            });
                            if (ds != null && ds.isOpen()) {
                                ds.close();
                            }
                        } catch (Exception ex) {
                            logError("Error loading itinerary tasks. " + ex.getMessage());
                        } finally {

                        }

                        builder.setView(view);
                        alertDialog = builder.show();
                    }
                });
    }

    class ItineraryAdapter extends ArrayAdapter<ItineraryProject> {
        private Context context;
        private int layoutResourceId;
        private ArrayList<ItineraryProject> data;

        public ItineraryAdapter(Context context, int layoutResourceId, ArrayList<ItineraryProject> data) {
            super(context, layoutResourceId, data);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            if (data.size() > 0) {
                ItineraryProject item = data.get(position);
                TextView tvProject = (TextView)  convertView.findViewById(R.id.tvProject);
                TextView tvTime = (TextView)  convertView.findViewById(R.id.tvTime);
                tvProject.setText(item.project);
                SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                    tvTime.setText(sdf.format(df.format(df.parse(item.timeIn))));
                } catch(Exception ex) {
                    tvTime.setText(item.timeIn);
                }
            }
            return convertView;
        }

        @Override
        public boolean isEmpty() {
            return data.size() == 0;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public void clear() {
            data = new ArrayList<>();
            data.clear();
            notifyDataSetChanged();
        }

        public void setData(ArrayList<ItineraryProject> data) {
            this.data = data;
            this.notifyDataSetChanged();
        }
    }

    private void showDoubleLogDialog(final TimeLog log, final String strPersistTime, final String photoPath, final NImage nImage, final String displayTime, final String logType) {
        DoubleLogDialogFragment df = new DoubleLogDialogFragment();
        df.setListener(new DoubleLogDialogFragment.DoubleLogDialogListener() {
            @Override
            public void onTimeIn(DialogFragment dialog) {
                if (nImage != null) {
                    try {
                        nImage.save(photoPath);
                    } catch (Exception ex) {
                        logError(ex.getMessage());
                    }
                }
                persistLog(log.getAccessCode(), photoPath, strPersistTime, displayTime, "IN");
                if (isEnableItinerary()) {
                    logItinerary(log.getAccessCode(), strPersistTime, "IN");
                }
            }

            @Override
            public void onTimeOut(DialogFragment dialog) {
                if (nImage != null) {
                    try {
                        nImage.save(photoPath);
                    } catch (Exception ex) {
                        logError(ex.getMessage());
                    }
                }

                persistLog(log.getAccessCode(), photoPath, strPersistTime, displayTime, "OUT");
                if (isEnableItinerary()) {
                    logItinerary(log.getAccessCode(), strPersistTime, "OUT");
                }
            }
        });
        df.setNImage(nImage);
        df.setTimeLog(log);
        df.show(getSupportFragmentManager(), "duplicate log type");
    }

    private void setEmployeeListAdapter() {
        edtAccessCode.setThreshold(1);
        EmployeeAutoCompleteAdapter adapter = new EmployeeAutoCompleteAdapter(FaceBundyActivity.this, R.layout.employee_adapter_item);
        edtAccessCode.setAdapter(adapter);
        edtAccessCode.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));

        edtAccessCode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EmployeeAdapterItem emp = (EmployeeAdapterItem) parent.getItemAtPosition(position);
                edtAccessCode.setText(emp.AccessCode);
            }
        });
    }

    public static class DoubleLogDialogFragment extends DialogFragment {
        public interface DoubleLogDialogListener {
            public void onTimeIn(DialogFragment dialog);
            public void onTimeOut(DialogFragment dialog);
        }

        private TextView tvPreviousLogName, tvPreviousLog;
        private ImageView imgPreviousLog, imgPreviousLogClose;
        private CircleButton btnConvertIn, btnConvertOut;
        private TimeLog log;
        private DoubleLogDialogListener listener;
        private NImage nImage;

        public void setListener(DoubleLogDialogListener listener) {
            this.listener = listener;
        }

        public void setNImage(NImage nImage) {
            this.nImage = nImage;
        }

        public void setTimeLog(TimeLog log) {
            this.log = log;
        }
        public DoubleLogDialogFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout to use as dialog or embedded fragment
            View view = inflater.inflate(R.layout.dialog_duplicate_log_type, container, false);
            btnConvertIn = (CircleButton) view.findViewById(R.id.btnConvertIn);
            btnConvertOut = (CircleButton) view.findViewById(R.id.btnConvertOut);
            tvPreviousLog = (TextView) view.findViewById(R.id.tvPreviousLog);
            tvPreviousLogName = (TextView) view.findViewById(R.id.tvPreviousLogName);
            imgPreviousLog = (ImageView) view.findViewById(R.id.imgPreviousLog);
            imgPreviousLogClose = (ImageView) view.findViewById(R.id.imgPreviousLogClose);

            if (log != null) {
                if (nImage != null) {
                    Bitmap bitmap = NImageUtils.bitmapFromNImage(nImage, getActivity());
                    imgPreviousLog.setImageBitmap(bitmap);
                }
                EmployeeDataSource ds = null;

                try {
                    ds = EmployeeDataSource.getInstance(getActivity());
                    ds.open();
                    Employee emp = ds.getEmployee(log.getAccessCode());
                    String name = null;
                    if (emp == null)
                        name = log.getAccessCode();
                    else
                        name = emp.getNickName().trim();
                    if (name.equals(""))
                        name = "fella";
                    tvPreviousLogName.setText(name + "!");
                } catch (Exception ex) {
                    Logger.logE(ex.getMessage());
                } finally {
                    if (ds != null)
                        ds.close();
                }
                String type = log.getType();
                String time = log.getTime();
                /*try {
                    time = StringUtils.getDaysPastInWords(log.getTime(), "dd/MM/yyyy hh:mm:ss a");
                } catch (Exception ex) {
                    Logger.logE(ex.getMessage());
                }*/

                String logDesc = String.format("You've already clocked %s last %s.",
                        type, time);
                tvPreviousLog.setText(logDesc);
            }
            imgPreviousLogClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DoubleLogDialogFragment.this.dismiss();
                }
            });

            btnConvertIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onTimeIn(DoubleLogDialogFragment.this);
                        DoubleLogDialogFragment.this.dismiss();
                    }
                }
            });
            btnConvertOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onTimeOut(DoubleLogDialogFragment.this);
                        DoubleLogDialogFragment.this.dismiss();
                    }
                }
            });
            return view;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // The only reason you might override this method when using onCreateView() is
            // to modify any dialog characteristics. For example, the dialog includes a
            // title by default, but your custom layout might not need it. So here you can
            // remove the dialog title, but you must call the superclass to get the Dialog.
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            return dialog;
        }
    }

    class CheckLastLogEntry extends AsyncTask<String, String, TimeLog> {
        private ObjectCallback<String, TimeLog> callback;
        private String accessCode;

        public CheckLastLogEntry(String accessCode, ObjectCallback<String, TimeLog> callback) {
            this.callback = callback;
            this.accessCode = accessCode;
        }

        @Override
        protected TimeLog doInBackground(String... params) {
            TimelogDataSource tds = TimelogDataSource.getInstance(FaceBundyActivity.this);
            TimeLog tl = null;
            try {
                tds.open();
                tl = tds.getLastTimeLogByAccessCode(accessCode);
            } catch(Exception ex) {
                callback.onObjectRequestError(ex.getMessage());
            } finally {
                tds.close();
            }
            return tl;
        }

        @Override
        protected void onPostExecute(TimeLog t) {
            super.onPostExecute(t);
            callback.onObjectRequestComplete(t);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callback.onPreRequest();
        }
    }


    public static final int SETTINGS_REQUEST = 0x00015;

    private void showSettings() {
        if (CacheManager.getInstance(this).getBooleanPreference("key_pref_enable_admin_pin")) {
            DialogFragment pin = PinCodeDialogFragment.newInstance(null, new DialogFragmentResultListener() {
                @Override
                public void onResultReturned(Object value) {
                    boolean valid = Boolean.parseBoolean(String.valueOf(value));
                    if (valid) {
                        Intent intent = new Intent(FaceBundyActivity.this, SettingsActivity.class);
                        startActivityForResult(intent, SETTINGS_REQUEST);
                    }
                }

                @Override
                public void onResultWithValueReturned(boolean valid, Object value) {

                }
            });
            pin.show(getSupportFragmentManager(), "request_pin_code");
        } else {
            Intent intent = new Intent(FaceBundyActivity.this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_REQUEST);
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void initBiometrics(final boolean startCapture) {
        mClient = Model.getInstance().getClient();
        mSubject = Model.getInstance().getSubject();
        mClient.getDeviceManager().setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));

        /*@TODO: SETTINGS FOR BIOMETRICS */
        String thres = CacheManager.getInstance(this).getStringPreference("pref_key_matching_threshold");
        if (thres.equals(""))
            thres = "24";
        int threshold = Integer.parseInt(thres);

        mClient.setMatchingThreshold(threshold);
        //mClient.setFacesCreateThumbnailImage(true);
        //mClient.setFacesThumbnailImageWidth(90);

        try {
            mClient.initialize();
            NCamera camera = (NCamera) connectDevice(mClient.getDeviceManager(), Source.CAMERA);
            mClient.setFaceCaptureDevice(camera);
            mAppClosing = false;

            mClient.list(NBiometricOperation.LIST, subjectListHandler);
        } catch(Exception ex) {}
        if (startCapture)
            startCapturing();
    }

    /* VeriLook Face Recognition */
    private final PropertyChangeListener biometricPropertyChanged = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if ("Status".equals(event.getPropertyName())) {
                final NBiometricStatus status = ((NBiometric) event.getSource()).getStatus();
                runOnUiThread(new Runnable() {
                    public void run() {

                    }
                });
            }
        }
    };

    private CompletionHandler<NSubject[], ? super NBiometricOperation> subjectListHandler = new CompletionHandler<NSubject[], NBiometricOperation>() {
        @Override
        public void completed(NSubject[] nSubjects, NBiometricOperation nBiometricOperation) {
            Model.getInstance().setSubjects(nSubjects);
        }

        @Override
        public void failed(Throwable throwable, NBiometricOperation nBiometricOperation) {
            logError("Error on subject list handler: " + throwable.getMessage());
        }
    };

    private CompletionHandler<NBiometricTask, NBiometricOperation> completionHandler = new CompletionHandler<NBiometricTask, NBiometricOperation>() {
        @Override
        public void completed(NBiometricTask nBiometricTask, NBiometricOperation nBiometricOperation) {
            final NBiometricStatus status = nBiometricTask.getStatus();
            onOperationCompleted(nBiometricOperation, nBiometricTask);
            switch (status) {
                case OPERATION_NOT_ACTIVATED:
                    logError("Operation not activated.");
                    break;
                case CANCELED:
                    return;
                default:
                    break;
            }

            switch (nBiometricOperation) {
                case DELETE:
                    if (status == NBiometricStatus.OK) {
                        logInfo("Deleted.");
                    } else {
                        logError("Deletion failed. " + status.toString());
                        showCrouton("Deletion failed. " + status.toString(), Style.ALERT);
                    }
                    mClient.list(NBiometricOperation.LIST, subjectListHandler);
                    startCapturing();
                    break;
                case CAPTURE:
                case CREATE_TEMPLATE:
                    if (status == NBiometricStatus.OK) {
                        logInfo("Extraction succeeded.");
                    } else {
                        logError("Extraction failed. " + status.toString());
                        switch (status) {
                            case BAD_DYNAMIC_RANGE:
                            case BAD_EXPOSURE:
                                DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.BadDynamicRange);
                                break;
                            case BAD_SHARPNESS:
                                DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.BadSharpness);
                                break;
                            case OBJECT_NOT_FOUND:
                                DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.BadFacePosture);
                                break;
                            default:
                                showCrouton("Extraction failed. " + status.toString(), Style.ALERT);
                        }
                        setCaptured(false);
                    }
                    break;
                case ENROLL:
                case ENROLL_WITH_DUPLICATE_CHECK:
                    final String msg;
                    if (status == NBiometricStatus.OK) {
                        msg = "Enrolment successful.";
                        logInfo(msg);

                        exportFaceTemplate();
                    } else {
                        msg = "Enrollment failed." + status.toString();
                        logError(msg);
                    }
                    mClient.list(NBiometricOperation.LIST, subjectListHandler);
                    setCaptured(false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bmp = BitmapFactory.decodeResource(FaceBundyActivity.this.getResources(), R.drawable.confused);
                            if (status == NBiometricStatus.DUPLICATE_ID || status == NBiometricStatus.DUPLICATE_FOUND) {
                                if (mCapturedSubject != null && status == NBiometricStatus.OK) {
                                    NImage nImg = mCapturedSubject.getFaces().get(0).getImage();
                                    bmp = NImageUtils.bitmapFromNImage(nImg, FaceBundyActivity.this);
                                }
                                DialogUtils.showDialog(FaceBundyActivity.this, "Face Enrollment", "Face Enrollment", "You're already enrolled", bmp, DialogUtils.DialogImageSize.OneByTwo, true, "Okay");
                            } else {
                                if (status == NBiometricStatus.OK) {
                                    if (mCapturedSubject != null) {
                                        NImage nImg = mCapturedSubject.getFaces().get(0).getImage();
                                        bmp = NImageUtils.bitmapFromNImage(nImg, FaceBundyActivity.this);
                                    }

                                    DialogUtils.showDialog(FaceBundyActivity.this, "Face Enrollment", "Enrollment Successful", msg, bmp, DialogUtils.DialogImageSize.OneByTwo, true, "Okay");
                                } else {
                                    switch (status) {
                                        case BAD_DYNAMIC_RANGE:
                                        case BAD_EXPOSURE:
                                            DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.BadDynamicRange);
                                            break;
                                        case BAD_SHARPNESS:
                                            DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.BadSharpness);
                                            break;
                                        case OBJECT_NOT_FOUND:
                                            DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.BadFacePosture);
                                            break;
                                        default:
                                            DialogUtils.showDialog(FaceBundyActivity.this, "Face Enrollment", "Enrollment Failed", msg, bmp, DialogUtils.DialogImageSize.OneByTwo, true, "Okay");
                                            break;
                                    }
                                }
                            }
                        }
                    });

                    break;
                case VERIFY:
                    if (isVerifyTest) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bmp = BitmapFactory.decodeResource(FaceBundyActivity.this.getResources(), R.drawable.cant_identify);
                                if (mCapturedSubject != null && status == NBiometricStatus.OK) {
                                    NImage nImg = mCapturedSubject.getFaces().get(0).getImage();
                                    bmp = NImageUtils.bitmapFromNImage(nImg, FaceBundyActivity.this);
                                }

                                if (status == NBiometricStatus.OK)
                                    DialogUtils.showDialog(FaceBundyActivity.this, "Verified Successfully", "Verified Successfully", "Face Recognized", bmp, DialogUtils.DialogImageSize.OneByTwo, true, "Okay");
                                else
                                    DialogUtils.showDialog(FaceBundyActivity.this, "Verification Failed", "Verification Failed", "Face Not Recognized", bmp, DialogUtils.DialogImageSize.OneByTwo, true, "Okay");
                                setCaptured(false);
                            }
                        });
                    } else {
                        boolean failed = true;
                        if (status == NBiometricStatus.OK) {
                            msg = "Verification successful.";
                            logInfo(msg);
                            failed = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Date logTime = getCurrentTime(false).getTime();
                                    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                                    final DateFormat dfd = new SimpleDateFormat("MM/dd/yyyy\nhh:mm:ss a");
                                    final String strTime = String.valueOf(dfd.format(logTime));

                                    persistLog(mSubject.getId(), "", df.format(logTime), strTime, clockType);
                                    if (isEnableItinerary()) {
                                        logItinerary(mSubject.getId(), df.format(logTime), clockType);
                                    }
                                }
                            });
                        } else {
                            msg = "Verification failed. " + status.toString();
                            logError(msg);
                        }

                        if (status == NBiometricStatus.MATCH_NOT_FOUND) {
                            DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.CantIdentify);
                            setCaptured(false);
                        } else if (status == NBiometricStatus.ID_NOT_FOUND || status == NBiometricStatus.INVALID_ID) {
                            DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.UnknownAccessCode);
                        } else if (status == NBiometricStatus.BAD_DYNAMIC_RANGE || status == NBiometricStatus.BAD_EXPOSURE) {
                            DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.BadDynamicRange);
                            setCaptured(false);
                        } else {
                            if (failed)
                                DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.Confused);
                            setCaptured(false);
                        }
                    }
                    break;
                case IDENTIFY:
                    if (status == NBiometricStatus.OK) {
                        StringBuilder sb = new StringBuilder();
                        NSubject subject = nBiometricTask.getSubjects().get(0);
                        for (NMatchingResult result : subject.getMatchingResults()) {
                            sb.append(result.getId()).append('\n');
                        }
                        logInfo("Identified: " + sb.toString());
                    } else {
                        logError("No match found. " + status.toString());
                    }
                    break;
                default:
                    throw new AssertionError("Invalid NBiometricOperation");
            }
        }

        @Override
        public void failed(Throwable throwable, NBiometricOperation nBiometricOperation) {
            logError("Error: " + throwable.getMessage() != null ? throwable.getMessage() : throwable.toString());
        }
    };

    private void exportFaceTemplate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSubject != null) {
                    NFace face = mSubject.getFaces().get(0);
                    File outputFaceFile = new File(GlobalConstants.getEmployeeFaceTemplatesDir(FaceBundyActivity.this),
                            edtAccessCode.getText().toString().trim().concat(".jpg"));
                    try {
                        face.getImage().save(outputFaceFile.getAbsolutePath());

                        outputFaceFile = new File(GlobalConstants.getEmployeeFaceTemplatesDir(FaceBundyActivity.this),
                                edtAccessCode.getText().toString().trim().concat(".dat"));
                        NFile.writeAllBytes(outputFaceFile.getAbsolutePath(), mSubject.getTemplateBuffer());
                        logDebug("Template written: " + outputFaceFile.getAbsolutePath());
                    } catch (Exception ex) {
                        logError(ex.getMessage());
                    }
                }
            }
        });
    }

    private void capture(NSubject subject) {
        if (subject == null) throw new NullPointerException("subject");
        mSubject = subject;
        NBiometricTask task = mClient.createTask(EnumSet.of(NBiometricOperation.CREATE_TEMPLATE), subject);
        mClient.performTask(task, NBiometricOperation.CREATE_TEMPLATE, completionHandler);
        onOperationStarted(NBiometricOperation.CAPTURE);
    }

    private void identify(String id) {
        if (mSubject == null) throw new NullPointerException("subject");
        NBiometricTask task = mClient.createTask(EnumSet.of(NBiometricOperation.IDENTIFY), mSubject);
        mClient.performTask(task, NBiometricOperation.IDENTIFY, completionHandler);
        onOperationStarted(NBiometricOperation.IDENTIFY);
    }

    private void createTemplate(NSubject subject) {
        if (subject == null) throw new NullPointerException("subject");
        mSubject = subject;
        NBiometricTask task = mClient.createTask(EnumSet.of(NBiometricOperation.CREATE_TEMPLATE), subject);
        mClient.performTask(task, NBiometricOperation.CREATE_TEMPLATE, completionHandler);
        onOperationStarted(NBiometricOperation.CREATE_TEMPLATE);
    }

    private void verify(String id) {
        if (mSubject == null) throw new NullPointerException("subject");
        mSubject.setId(id);
        mCapturedSubject = mSubject;
        NBiometricTask task = mClient.createTask(EnumSet.of(NBiometricOperation.VERIFY), mSubject);
        mClient.performTask(task, NBiometricOperation.VERIFY, completionHandler);
        onOperationStarted(NBiometricOperation.VERIFY);
    }

    private void verifyFromDatabase(final String accessCode) {
        if (mSubject == null) throw new NullPointerException("subject");
        try {
            NSubject reference = createSubjectFromFile(accessCode);
            if (reference != null) {
                mClient.verify(reference, mSubject, null, new CompletionHandler<NBiometricStatus, Object>() {
                    @Override
                    public void completed(NBiometricStatus result, Object o) {
                        if (result == NBiometricStatus.OK) {
                            boolean hasMatch = false;
                            for (NMatchingResult matchResult : mSubject.getMatchingResults()) {
                                hasMatch = true;
                                logInfo("Matching score: " + String.valueOf(matchResult.getId()) + ", " + String.valueOf(matchResult.getScore()));
                                if (matchResult.getMatchingDetails() != null) {
                                    StringBuilder sb = new StringBuilder();
                                    NMatchingDetails details = matchResult.getMatchingDetails();
                                    if (details.getBiometricType().contains(NBiometricType.FACE)) {
                                        sb.append("FACE MATCHING DETAILS: ");
                                        sb.append("Score = " + String.valueOf(details.getFacesScore()));
                                        for (NLMatchingDetails faceDetails : details.getFaces()) {
                                            sb.append("FACE INDEX SCORE: " + String.valueOf(faceDetails.getMatchedIndex())
                                                    + " FACE DETAILS SCORE: " + String.valueOf(faceDetails.getScore()));
                                        }
                                    }
                                    logInfo(sb.toString());
                                }
                            }
                            if (hasMatch) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Date logTime = getCurrentTime(false).getTime();
                                        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                                        final DateFormat dfd = new SimpleDateFormat("MM/dd/yyyy\nhh:mm:ss a");
                                        final String strTime = String.valueOf(dfd.format(logTime));

                                        persistLog(accessCode, "", df.format(logTime), strTime, clockType);
                                        if (isEnableItinerary()) {
                                            logItinerary(accessCode, df.format(logTime), clockType);
                                        }
                                    }
                                });
                                showCrouton("Verification successful.", Style.INFO);
                            }
                        } else {
                            logError("Failed: " + result.toString());
                            if (result.toString().equals("MATCH_NOT_FOUND")) {
                                showCrouton("Sorry we can't identify your face. Please make sure you have entered the correct access code.", Style.ALERT);
                            } else {
                                showCrouton("Failed: " + result.toString(), Style.ALERT);
                            }
                        }
                        setCaptured(false);
                    }

                    @Override
                    public void failed(Throwable throwable, Object o) {
                        logError("Verification failed.");
                        setCaptured(false);
                    }
                });
            } else {
                logError("Reference subject is null.");
                //showCrouton("Reference subject is null.", Style.ALERT);
                setCaptured(false);
            }
        } catch (Exception ex) {
            logError(ex.getMessage());
            showCrouton(ex.getMessage(), Style.ALERT);
            setCaptured(false);
        }
    }

    private void verifyFromFile(String accessCode) {
        if (mSubject == null) throw new NullPointerException("subject");
        try {
            NSubject reference = createSubjectFromFile(accessCode);
            if (reference != null) {
                mClient.verify(reference, mSubject, null, new CompletionHandler<NBiometricStatus, Object>() {
                    @Override
                    public void completed(NBiometricStatus result, Object o) {
                        if (result == NBiometricStatus.OK) {
                            for (NMatchingResult matchResult : mSubject.getMatchingResults()) {
                                logInfo("Matching score: " + String.valueOf(matchResult.getId()) + ", " + String.valueOf(matchResult.getScore()));
                                if (matchResult.getMatchingDetails() != null) {
                                    StringBuilder sb = new StringBuilder();
                                    NMatchingDetails details = matchResult.getMatchingDetails();
                                    if (details.getBiometricType().contains(NBiometricType.FACE)) {
                                        sb.append("FACE MATCHING DETAILS: ");
                                        sb.append("Score = " + String.valueOf(details.getFacesScore()));
                                        for (NLMatchingDetails faceDetails : details.getFaces()) {
                                            sb.append("FACE INDEX SCORE: " + String.valueOf(faceDetails.getMatchedIndex())
                                                    + " FACE DETAILS SCORE: " + String.valueOf(faceDetails.getScore()));
                                        }
                                    }
                                    logInfo(sb.toString());
                                }
                            }
                        } else {
                            logError("Failed: " + result.toString());
                        }
                    }

                    @Override
                    public void failed(Throwable throwable, Object o) {
                        logError("Verification failed.");
                    }
                });
            } else {
                logError("Reference subject is null.");
            }
        } catch (Exception ex) {
            logError(ex.getMessage());
        }
    }

    private NSubject createSubjectFromFile(String accessCode) throws IOException {
        NSubject subject = new NSubject();
        NFace face = new NFace();
        String filename = GlobalConstants.getEmployeeFaceTemplatesDir(this) + "/" + accessCode + ".jpg";
        File f = new File(filename);
        if (!f.exists()) {
            DialogUtils.showBiometricCaptureInfo(FaceBundyActivity.this, DialogUtils.BiometricInfoType.CantIdentify);
            //showCrouton("Sorry we can't identify your face. Please make sure you have entered the correct access code.", Style.ALERT);
            return null;
        }
        NImage image = NImage.fromFile(filename, NImageFormat.getJPEG());
        face.setImage(image);
        subject.getFaces().add(face);
        return subject;
    }

    private NSubject createSubjectFromImageFromDatabase(String accessCode) {
        NSubject subject = new NSubject();
        byte[] buffer;
        SubjectsDataSource ds = SubjectsDataSource.getInstance(this);
        try {
            if (!ds.isOpen())
                ds.open();
            Subject dsSubject = ds.getSubject(accessCode);
            buffer = dsSubject.getThumbnail();
            NFace face = new NFace();
            NImage image = NImage.fromMemory(NBuffer.fromArray(buffer));
            face.setImage(image);
            subject.getFaces().add(face);

        } catch(Exception ex) {
            logError("Error creating subject from template. " + ex.getMessage());
        } finally {
            if (ds.isOpen())
                ds.close();
        }

        return subject;
    }

    private NSubject createSubjectFromTemplateFromDatabase(String accessCode) {
        NSubject subject = new NSubject();
        byte[] buffer;
        SubjectsDataSource ds = SubjectsDataSource.getInstance(this);
        try {
            if (!ds.isOpen())
                ds.open();
            Subject dsSubject = ds.getSubject(accessCode);
            buffer = dsSubject.getTemplate();
            subject.setTemplateBuffer(NBuffer.fromArray(buffer));
            subject.setId(accessCode);
        } catch(Exception ex) {
            logError("Error creating subject from template. " + ex.getMessage());
        } finally {
            if (ds.isOpen())
                ds.close();
        }

        return subject;
    }

    public void extract(NSubject subject) {
        if (subject == null) throw new NullPointerException("subject");
        mSubject = subject;
        NBiometricTask task = mClient.createTask(EnumSet.of(NBiometricOperation.CREATE_TEMPLATE), subject);
        mClient.performTask(task, NBiometricOperation.CREATE_TEMPLATE, completionHandler);
        onOperationStarted(NBiometricOperation.CREATE_TEMPLATE);
    }

    public void enroll(String id) {
        mSubject.setId(id);
        NBiometricOperation operation = checkDuplicates ? NBiometricOperation.ENROLL_WITH_DUPLICATE_CHECK : NBiometricOperation.ENROLL;
        NBiometricTask task = mClient.createTask(EnumSet.of(operation), mSubject);
        mClient.performTask(task, NBiometricOperation.ENROLL, completionHandler);
        onOperationStarted(NBiometricOperation.ENROLL);
    }

    public void delete(String id) {
        mSubject.setId(id);
        NBiometricOperation operation = NBiometricOperation.DELETE;
        NBiometricTask task = mClient.createTask(EnumSet.of(operation), mSubject);
        mClient.performTask(task, NBiometricOperation.DELETE, completionHandler);
        onOperationStarted(NBiometricOperation.DELETE);
    }

    private void startCapturing() {
        mStatus = Status.CAPTURING;
        if (mFaceView != null) {
            NSubject subject = new NSubject();
            NFace face = new NFace();
            //face.addPropertyChangeListener(biometricPropertyChanged);
            face.setCaptureOptions(EnumSet.of(NBiometricCaptureOption.MANUAL));
            mFaceView.setFace(face);
            subject.getFaces().add(face);
            capture(subject);
        }
    }

    protected void onOperationStarted(NBiometricOperation operation) {
        if (operation == NBiometricOperation.CAPTURE) {
            mStatus = Status.CAPTURING;
        }
    }

    protected void onOperationCompleted(final NBiometricOperation operation, final NBiometricTask task) {
        if (operation == NBiometricOperation.CREATE_TEMPLATE && task.getStatus() == NBiometricStatus.OK) {
            mStatus = Status.TEMPLATE_CREATED;
        }

        if (task == null || (operation == NBiometricOperation.CREATE_TEMPLATE
                && task.getStatus() != NBiometricStatus.OK
                && task.getStatus() != NBiometricStatus.CANCELED
                && task.getStatus() != NBiometricStatus.OPERATION_NOT_ACTIVATED)) {
            //startCapturing();
        }
    }


    @Override
    public void onClockIn(String type) {
        clockType = type;
        tagGPSLocation();
    }

    protected void onStartCapturing() {
        stop();
    }

    protected void onStopCapturing() {
        cancel();
    }

    protected void stop() {
        if (mClient != null)
            mClient.force();
    }

    protected void cancel() {
        if (mClient != null)
            mClient.cancel();
    }

    protected void onLoad() {
        cancel();
    }

    protected boolean isStopSupported() {
        return false;
    }

    public void onSwitchCamera() {
        cancel();
        NCamera currentCamera = mClient.getFaceCaptureDevice();
        for (NDevice device : mClient.getDeviceManager().getDevices()) {
            if (device.getDeviceType().contains(NDeviceType.CAMERA)) {
                if (!device.equals(currentCamera)) {
                    mClient.setFaceCaptureDevice((NCamera) device);
                    startCapturing();
                    break;
                }
            }
        }
        setCaptured(false);
    }

    private void showEnrolledFaces() {
        Bundle bundle = new Bundle();
        EnrolledFacesDialogFragment.newInstance(Model.getInstance().getSubjects(), bundle, new DialogFragmentResultListener() {
            @Override
            public void onResultReturned(Object value) {
                if (value != null && !value.toString().equals("")) {
                    FaceBundyActivity.this.delete(value.toString());
                    FaceBundyActivity.this.showCrouton("Face unregistered: " + value.toString(), Style.ALERT);
                }
            }

            @Override
            public void onResultWithValueReturned(boolean valid, Object value) {

            }
        }).show(getSupportFragmentManager(), "enrolled_face_ids");
    }

    class GetNetworkTimeAsync extends AsyncTask<Void, Void, Long> {
        private ObjectCallback<Void, Long> callback;

        public GetNetworkTimeAsync(ObjectCallback<Void, Long> callback) {
            this.callback = callback;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            try {
                CacheManager cm = CacheManager.getInstance(FaceBundyActivity.this);
                int timeout = Integer.parseInt(cm.getStringPreference("pref_key_ntp_timeout")) * 1000 * 60;
                String server = cm.getStringPreference("pref_key_ntp_server");
                long networkTime = TimeUtils.getCurrentNetworkTime(timeout, server);
                logInfo("Internet time fetched.");
                return networkTime;
            } catch (Exception ex) {
                callback.onObjectRequestError("Error fetching internet time. " + ex.getMessage());
                return 0L;
            }
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            callback.onObjectRequestComplete(aLong);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callback.onPreRequest();
        }
    }

    private int getNTPInterval() {
        String interval = CacheManager.getInstance(FaceBundyActivity.this).getStringPreference("ntp_auto_sync_interval");
        int minTime = 30000 * 60 * 1;
        if (interval.equals("1")) {
            minTime = (1000 * 60) * 60;
        } else if (interval.equals("2")) {
            minTime = ((1000 * 60) * 60) * 2;
        } else if (interval.equals("3")) {
            minTime = ((1000 * 60) * 60) * 4;
        }
        return minTime;
    }

    private Calendar getCurrentTime(boolean force) {
        CacheManager cm = CacheManager.getInstance(this);
        Calendar calendar;
        if ((!cm.containsPreference(CacheManager.SERVER_TIME) || !cm.containsPreference(CacheManager.ELAPSED_TIME)) || force) {
            calendar = getTimeViaInternet();
        } else {
            long elapsed = cm.getLongPreference(CacheManager.ELAPSED_TIME);
            long realtime = SystemClock.elapsedRealtime();
            long difftime = realtime - elapsed;
            if (difftime < 0)
                calendar = getTimeViaInternet();
            else
                calendar = getTimeViewPreferences();
        }
        return calendar;
    }

    private Calendar getTimeViaInternet() {
        final Context context = this;
        final Calendar calSavedServerTime = Calendar.getInstance();
        final Calendar calSavedElapsedTime = Calendar.getInstance();
        final Calendar calCurrentTime = Calendar.getInstance();

        new GetNetworkTimeAsync(new ObjectCallback<Void, Long>() {
            @Override
            public Long onObjectRequestProcess(Void... params) {
                return null;
            }

            @Override
            public void onObjectRequestComplete(Long result) {
                CacheManager cm = CacheManager.getInstance(context);
                if (result != 0L) {
                    long serverTime, elapsedTime;
                    serverTime = result;
                    elapsedTime = SystemClock.elapsedRealtime();

                    calSavedServerTime.setTimeInMillis(serverTime);
                    calSavedElapsedTime.setTimeInMillis(elapsedTime);

                    cm.setPreference(CacheManager.SERVER_TIME, serverTime);
                    cm.setPreference(CacheManager.ELAPSED_TIME, elapsedTime);
                    cm.setPreference(CacheManager.TIME_LAST_SYNC_SOURCE, GlobalConstants.NTP_SYNC_SOURCE);
                    long mRealtime = SystemClock.elapsedRealtime();
                    long mDiffTime = mRealtime - elapsedTime;
                    long mCurrentTime = mDiffTime + serverTime;
                    calCurrentTime.setTimeInMillis(mCurrentTime);

                    Calendar elapsed = Calendar.getInstance();
                    elapsed.setTimeInMillis(SystemClock.elapsedRealtime());
                    Toast.makeText(FaceBundyActivity.this, "Time request successful. " + new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(calCurrentTime.getTime()), Toast.LENGTH_LONG).show();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onObjectRequestError(String message) {
                CacheManager cm = CacheManager.getInstance(context);
                cm.removePreference(CacheManager.SERVER_TIME);
                cm.removePreference(CacheManager.ELAPSED_TIME);
                logError(message + " --> App time cleared. Needs resync.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(View.GONE);
                    }
                });

            }

            @Override
            public void onRequestCancelled() {

            }

            @Override
            public void onPreRequest() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(View.VISIBLE);
                    }
                });

                logError("Requesting time from remote server...");
                CacheManager cm = CacheManager.getInstance(context);
                cm.removePreference(CacheManager.SERVER_TIME);
                cm.removePreference(CacheManager.ELAPSED_TIME);
            }
        }).execute();
        return calCurrentTime;
    }


    private Calendar getTimeViewPreferences() {
        CacheManager cm = CacheManager.getInstance(this);
        long mElapsed = cm.getLongPreference(CacheManager.ELAPSED_TIME);
        long mServerTime = cm.getLongPreference(CacheManager.SERVER_TIME);
        long mRealtime = SystemClock.elapsedRealtime();
        long mDiffTime = mRealtime - mElapsed;
        long mCurrentTime = mDiffTime + mServerTime;
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(mCurrentTime);
        return mCalendar;
    }

    @Override
    public void onBackPressed() {
        if (faceCaptured) {
            if (getAppEdition() != AppEditions.Basic) {
                //startCapturing();
                setCaptured(false);
            }
        } else {
            super.onBackPressed();
            mAppClosing = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getAppEdition() != AppEditions.Basic) {
            cancel();
            if (mAppClosing)
                LicensingManager.getInstance().release(Arrays.asList(LICENSES));
        }
    }

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

    @Override
    public void onLicensingStateChanged(final LicensingState state) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (state) {
                    case OBTAINING:
                        logInfo("Obtaining VeriLook license...");
                        progressDialog = ProgressDialog.show(context, "", getString(R.string.msg_initializing));
                        break;
                    case OBTAINED:
                        logInfo("VeriLook license obtained.");
                        mLicensesObtained = true;
                        initComponents();
                        progressDialog.dismiss();
                        break;
                    case NOT_OBTAINED:
                        logError("Failed to obtain VeriLook license.");
                        mLicensesObtained = false;
                        progressDialog.dismiss();
                        break;
                    default:
                        progressDialog.dismiss();
                        throw new AssertionError("Unknown state: " + state);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_face_bundy, menu);
        boolean flag = getAppEdition() != AppEditions.Basic;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            copyDb();
            showSettings();
            return true;
        } else if (id == R.id.action_itineraries) {
            showItineraries();
            return true;
        } else if (id == R.id.action_switch_camera) {
            switchCamera();
            return true;
        } else if (id == R.id.action_show_my_logs) {
            showTimeLogs(edtAccessCode.getText().toString(), false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showItineraries() {
        String accessCode = edtAccessCode.getText().toString().trim();
        if (accessCode.length() == 0) {
            DialogUtils.showBiometricCaptureInfo(this, DialogUtils.BiometricInfoType.UnspecifiedAccessCode);
            return;
        }
        Intent intent = new Intent(this, ItineraryProjectListActivity.class);
        intent.putExtra("ACCESS_CODE", accessCode);
        startActivity(intent);
    }

    private void copyDb(){

        String db = GlobalConstants.getInstance().getContext().getDatabasePath("FaceTimeLogs.db").getAbsolutePath();
        logError(db);
        String dest_db = GlobalConstants.getCacheDir(this) + "/FaceTimeLogs.db";
        File src = new File(db);
        File dest = new File(dest_db);
        try {
            FileUtils.copyFile(src, dest, true);
            logInfo("Database copied.");
        } catch (Exception ex) {
            logError(ex.getMessage());
        }
    }

    private void tagGPSLocation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GPSTracker gps = new GPSTracker(FaceBundyActivity.this);
                if (gps.canGetLocation()) {
                    Location location = gps.getLocation();
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                    if (ConnectivityHelper.isConnected(FaceBundyActivity.this))
                        new GeoCodeAsync().execute();
                }
            }
        });
    }

    public static void deleteFiles(String path) {

        File file = new File(path);

        if (file.exists()) {
            String deleteCmd = "rm -r " + path;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) { }
        }
    }


    private void showTimeLogs(String accessCode, boolean all) {
        Intent intent = new Intent(this, LogsReportActivity.class);
        if (!accessCode.isEmpty())
            intent.putExtra("ACCESS_CODE", accessCode);
        intent.putExtra("ALL", all);
        startActivity(intent);
    }

    private void enrollment() {
        if (getAppEdition() == AppEditions.Basic) {
            showCrouton("Sorry, this feature is not available in this edition.", Style.ALERT);
            //showSnackBar("Sorry, this feature is not available in this edition.", "", null);
            return;
        }

        String accessCode = edtAccessCode.getText().toString().trim();
        if (accessCode.length() == 0) {
            DialogUtils.showBiometricCaptureInfo(this, DialogUtils.BiometricInfoType.UnspecifiedAccessCode);
        } else {
            enroll(accessCode);
        }
    }

    private AppEditions getAppEdition(){
        return GlobalConstants.getInstance().getAppEdition();
    }

    private NSubject mCapturedSubject = null;

    private void persistLog(final String accessCode, final String filePath, final String time, final String displayTime,  final String type) {
        new PersistLogAsyncTask(this.getApplicationContext(), new ObjectCallback<String, Object>() {
            @Override
            public Object onObjectRequestProcess(String... params) {
                return null;
            }

            @Override
            public void onObjectRequestComplete(Object result) {
                if (result != null) {
                    TimeLog tl = (TimeLog) result;
                    logDebug(tl.getAccessCode() + ", " + tl.getTime());
                    if (mCapturedSubject != null) {
                        NImage nImg = mCapturedSubject.getFaces().get(0).getImage();
                        Bitmap bmp = NImageUtils.bitmapFromNImage(nImg, FaceBundyActivity.this);
                        imgPreview.setImageBitmap(bmp);
                    }
                    /*File file = new File(filePath);
                    if (file.exists()) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
                        imgPreview.setImageBitmap(bitmap);
                    }*/
                    tvNetworkTime.setText(displayTime);
                    tvNetworkTime.setTextColor(getResources().getColor(type.equals("IN") ? R.color.green : R.color.red));
                    displayEmployeeName(accessCode);
                    edtAccessCode.setText("");
                    llPreview.setVisibility(View.VISIBLE);
                    toggleDateTimeWarning(View.GONE);
                    CacheManager.getInstance(FaceBundyActivity.this).setPreference(CacheManager.HIDE_DATETIME_WARNING, true);
                    Animation fadeInAnim = AnimationUtils.loadAnimation(FaceBundyActivity.this, R.anim.anim_fade_in);
                    fadeInAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            llPreview.setVisibility(View.VISIBLE);
                            if (mCapturedSubject != null) {
                                NImage nImg = mCapturedSubject.getFaces().get(0).getImage();
                                Bitmap bmp = NImageUtils.bitmapFromNImage(nImg, FaceBundyActivity.this);
                                if (bmp != null)
                                    DialogUtils.showDialog(FaceBundyActivity.this, "Time Log", "Thank you, " + getNickName(accessCode) + ".\nYour time " + type + " was acccepted.",
                                            "Time Log: " + displayTime, bmp, DialogUtils.DialogImageSize.OneByTwo, true, "Okay");
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    llPreview.startAnimation(fadeInAnim);
                    if (CacheManager.getInstance(FaceBundyActivity.this).getBooleanPreference(CacheManager.ENABLE_REALTIME_UPLOAD))
                        uploadTimeLogs();
                } else {
                    showCrouton("Timelog is null.", Style.ALERT);
                }

                if (getAppEdition() == AppEditions.ExtractionOnly) {

                    setCaptured(true);
                }
            }

            @Override
            public void onObjectRequestError(String message) {
                showCrouton(message, Style.ALERT);
                if (getAppEdition() == AppEditions.ExtractionOnly) {
                    setCaptured(false);
                }
            }

            @Override
            public void onRequestCancelled() {
                if (getAppEdition() == AppEditions.ExtractionOnly) {
                    setCaptured(false);
                }
            }

            @Override
            public void onPreRequest() {

            }
        }).execute(accessCode, time, type, filePath, String.valueOf(AppEditions.toInt(getAppEdition())));
    }

    private String getNickName(String accessCode) {
        EmployeeDataSource ds = null;
        String name = accessCode;
        try {
            ds = EmployeeDataSource.getInstance(this);
            ds.open();
            Employee emp = ds.getEmployee(accessCode);
            if (emp != null) {
                name = emp.getNickName();
            }
        } catch (Exception ex) {
            logError(ex.getMessage());
        } finally {
            if (ds != null)
                ds.close();
        }
        return name;
    }

    private void displayEmployeeName(String accessCode) {
        EmployeeDataSource ds = null;
        String name = accessCode;
        try {
            ds = EmployeeDataSource.getInstance(this);
            ds.open();
            Employee emp = ds.getEmployee(accessCode);
            if (emp != null) {
                name = emp.getName();
            }
        } catch (Exception ex) {
            logError(ex.getMessage());
        } finally {
            if (ds != null)
                ds.close();
        }

        tvEmployeeName.setText(name);
    }

    private void uploadTimeLogs() {
        if(ConnectivityHelper.isConnected(this)) {
            final TimeLogUploadHelper helper = TimeLogUploadHelper.getInstance(this);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] hosts = CacheManager.getInstance(FaceBundyActivity.this).getStringPreference(CacheManager.SERVER_HOSTS).split("\n"); //new String[] {"http://10.0.0.82:3003", "http://activation.facebundy.com"};
                    new ReachableServerHost(FaceBundyActivity.this, hosts, new ReachableServerHostListener() {
                        @Override
                        public void onStatusChanged(ServerHostStatus status, String host) {
                            logDebug(status.toString() + ": " + host);
                        }

                        @Override
                        public void onReachableHostAcquired(String reachableHost) {
                            helper.upload(reachableHost);
                        }

                        @Override
                        public void onFailedHostAcquisition(String message) {
                            showCrouton("Time log will not be uploaded this time. " + message, Style.ALERT);
                        }
                    }).execute();
                }
            });
        } else {
            showCrouton("Please connect to the internet to upload time logs.", Style.ALERT);
        }
    }

    private TimerTask timerTask;

    private void setClock() {
        final Handler handler = new Handler();
        Timer callTimer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        initClockTime();
                    }
                });
            }
        };
        callTimer.schedule(timerTask, 0, 1000);
    }

    private void initClockTime() {
        if (tvAppDate != null && tvAppTime != null) {
            try {
                long elapsed = CacheManager.getInstance(this).getLongPreference(CacheManager.ELAPSED_TIME);
                long realtime = SystemClock.elapsedRealtime();
                long difftime = realtime - elapsed;
                if (difftime < 0) {
                    tvAppDate.setText("Clock out of sync.");
                    tvAppTime.setText("");
                } else {
                    Calendar cal = getTimeViewPreferences();
                    SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");
                    tvAppDate.setText(df.format(cal.getTime()));
                    df = new SimpleDateFormat("hh:mm:ss a");
                    tvAppTime.setText(df.format(cal.getTime()));
                    notifyDeviceTimeNotSynchronized(cal);
                }
                String source = CacheManager.getInstance(FaceBundyActivity.this).getStringPreference(CacheManager.TIME_LAST_SYNC_SOURCE);
                source = source.equals("") ? "Unknown" : source;
                tvLastSync.setText("last synched from " + source);
            } catch (Exception ex) {
                tvAppDate.setText("Clock out of sync.");
            }
        }
    }

    private GPSTracker gpsTracker;

    private void setGpsTime(boolean force) {
        if (CacheManager.getInstance(this).getBooleanPreference("pref_key_enable_auto_sync_gps") || force) {
            gpsTracker = new GPSTracker(this);
            gpsTracker.setUseNetWorkTime(false);
            gpsTracker.setGpsTrackerListener(new GPSTracker.GpsTrackerListener() {
                @Override
                public void onLocationChange(Location location) {
                    if (location != null) {
                        long elapsedTime = SystemClock.elapsedRealtime();
                        long currentTime = location.getTime();
                        CacheManager.getInstance(FaceBundyActivity.this).setPreference(CacheManager.ELAPSED_TIME, elapsedTime);
                        CacheManager.getInstance(FaceBundyActivity.this).setPreference(CacheManager.SERVER_TIME, currentTime);
                        CacheManager.getInstance(FaceBundyActivity.this).setPreference(CacheManager.TIME_LAST_SYNC_SOURCE, GlobalConstants.GPS_SYNC_SOURCE);
                        //Toast.makeText(FaceBundyActivity.this, "GPS time acquired.", Toast.LENGTH_SHORT).show();
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        logError("GPS: " + location.toString());
                    } else {
                        logError("GPS: null");
                    }
                }

                @Override
                public void onStatusChange(String provider, int status, Bundle extras) {
                    logError("GPS: " + provider + ", Status: " + status);
                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onLocateStarted() {
                }

                @Override
                public void onFinish(final String message) {

                }
            });
            Location location = gpsTracker.getLocation();
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        }
    }

    private int timeMessageVisibility = View.GONE;

    private void notifyDeviceTimeNotSynchronized(Calendar cal) {
        Calendar cur = Calendar.getInstance();
        long diff = Math.abs(cur.getTimeInMillis() - cal.getTimeInMillis());

        if ((diff >= 5 * 60 * 1000)) {
            if (timeMessageVisibility == View.GONE) {
                if (CacheManager.getInstance(this).getBooleanPreference(CacheManager.HIDE_DATETIME_WARNING)) {
                    imgDateTimeWarningCompact.setVisibility(View.VISIBLE);
                    //llapp_date_time_container.setBackgroundColor(0);
                } else {
                    toggleDateTimeWarning(View.VISIBLE);
                    //llapp_date_time_container.setBackgroundColor(getResources().getColor(R.color.transparent_royal_blue));
                    imgDateTimeWarningCompact.setVisibility(View.GONE);
                }
            } else {
                llapp_date_time_container.setBackgroundColor(getResources().getColor(R.color.transparent_royal_blue));
                imgDateTimeWarningCompact.setVisibility(View.GONE);
            }
        } else {
            if (timeMessageVisibility == View.VISIBLE) {
                toggleDateTimeWarning(View.GONE);
                //llapp_date_time_container.setBackgroundColor(0);
                imgDateTimeWarningCompact.setVisibility(View.GONE);
            }
        }
    }

    private void toggleDateTimeWarning(final int visibility) {
        this.timeMessageVisibility = visibility;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (visibility == View.VISIBLE) {
                    rlDateTime.setVisibility(View.VISIBLE);
                }
                Animation fade = AnimationUtils.loadAnimation(FaceBundyActivity.this, visibility == View.GONE ? R.anim.anim_fade_out : R.anim.anim_fade_in);
                fade.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (visibility == View.VISIBLE) {
                            hidePreview();
                        }
                        rlDateTime.setVisibility(visibility);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                rlDateTime.startAnimation(fade);
            }
        });
    }

    class PersistLogAsyncTask extends AsyncTask<String, Void, Object> {
        private ObjectCallback<String, Object> callback;
        private Context context;

        public PersistLogAsyncTask(Context context, ObjectCallback<String, Object> callback) {
            this.callback = callback;
            this.context = context;
        }

        @Override
        protected Object doInBackground(String... params) {
            TimelogDataSource DS = TimelogDataSource.getInstance(context);
            TimeLog log = null;
            try {
                DS.open();
                log = DS.createTimeLog(params[0], params[1], params[2], params[3], Integer.parseInt(params[4]), latitude, longitude);
            } catch (Exception e) {
                callback.onObjectRequestError(e.getMessage());
            } finally {
                DS.close();
            }
            return log;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callback.onPreRequest();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            callback.onObjectRequestComplete(o);
        }
    }

    private float mBatteryPercentage;

    private final BroadcastReceiver mBatteryStatusReceiver = new BatteryLevelReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            mBatteryPercentage = level / (float)scale;
            int percentage = (int) (mBatteryPercentage * 100.0f);
            if (rlBatteryStatus != null) {
                if (percentage > 10) {
                    rlBatteryStatus.setVisibility(View.GONE);
                } else {
                    rlBatteryStatus.setVisibility(View.VISIBLE);
                    tvBatteryDesc.setText(percentage + "% remaining battery life.\nPlease connect your charger now.");
                }
            }
        }
    };

    private void monitorBatteryStatus() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(mBatteryStatusReceiver, filter);
    }
}
