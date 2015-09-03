package com.jeonsoft.facebundypro;

/**
 * Created by WendellWayne on 2/24/2015.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jeonsoft.facebundypro.uploadservice.AbstractUploadServiceReceiver;
import com.jeonsoft.facebundypro.uploadservice.UploadService;
import com.jeonsoft.facebundypro.utils.ObjectCallback;
import com.jeonsoft.facebundypro.views.BigPreview;
import com.jeonsoft.facebundypro.views.CameraFacePreview;
import com.jeonsoft.facebundypro.views.CameraManager;
import com.jeonsoft.facebundypro.views.CircleButton;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoCaptureActivity extends ActionBarActivity {

    private CameraFacePreview cfp;
    private ImageView img;
    private String filename;
    private TextView tvNetworkTime;
    private EditText edt;
    private boolean capturing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UploadService.NAMESPACE = "com.jeonsoft";

        setContentView(R.layout.activity_face_clock);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CircleButton cb = (CircleButton) findViewById(R.id.btnCaptureIn);
        tvNetworkTime = (TextView) findViewById(R.id.tvNetworkTime);
        cfp = (CameraFacePreview) findViewById(R.id.surface);
        img = (ImageView) findViewById(R.id.imgImagePreview);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), BigPreview.class);
                intent.putExtra("filename", filename);
                startActivity(intent);
            }
        });
        final Activity activity = this;
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturing = true;
                CameraManager.getInstance().getCamera().takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {

                    }
                }, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                        final String strTime = String.valueOf(df.format(new Date(System.currentTimeMillis())));

                        new SavePhotoTask(new ObjectCallback<Void, String>() {
                            @Override
                            public String onObjectRequestProcess(Void... params) {
                                return null;
                            }

                            @Override
                            public void onObjectRequestComplete(String result) {
                                if (result != null) {
                                    cfp.setPreviewImagePath(result);
                                    CameraManager.getInstance().getCamera().startPreview();
                                    cfp.invalidate();

                                    File file = new File(result);
                                    if (file.exists()) {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                        Bitmap bitmap = BitmapFactory.decodeFile(result, options);
                                        img.setImageBitmap(bitmap);
                                        filename = result;
                                        tvNetworkTime.setText(strTime);
                                        tvNetworkTime.setTextColor(getResources().getColor(R.color.green));
                                        //persistLog(file.getPath(), strTime, "IN");
                                    }
                                } else {
                                    CameraManager.getInstance().getCamera().startPreview();
                                }
                                capturing = false;
                            }

                            @Override
                            public void onObjectRequestError(String message) {
                                Log.e("Jeonsoft", "Error: " + message);
                                CameraManager.getInstance().getCamera().startPreview();
                            }

                            @Override
                            public void onRequestCancelled() {
                                CameraManager.getInstance().getCamera().startPreview();
                            }

                            @Override
                            public void onPreRequest() {
                            }
                        }, strTime, edt.getText().toString()).execute(bytes);
                    }
                });
            }
        });

        CircleButton cbo = (CircleButton) findViewById(R.id.btnCaptureOut);
        cbo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*DataCacheManager dm = DataCacheManager.getInstance(getApplicationContext());
                if (!dm.containsPreference(DataCacheManager.DEVICE_GUID)) {
                    getTokenAndRegisterDevice();
                } else {
                    Log.e("Jeonsoft", "Preference exists. " + dm.getStringPreference(DataCacheManager.DEVICE_GUID));
                }*/
                capturing = true;
                CameraManager.getInstance().getCamera().takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {

                    }
                }, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                        final String strTime = String.valueOf(df.format(new Date(System.currentTimeMillis())));

                        new SavePhotoTask(new ObjectCallback<Void, String>() {
                            @Override
                            public String onObjectRequestProcess(Void... params) {
                                return null;
                            }

                            @Override
                            public void onObjectRequestComplete(String result) {
                                if (result != null) {
                                    cfp.setPreviewImagePath(result);
                                    CameraManager.getInstance().getCamera().startPreview();
                                    cfp.invalidate();

                                    File file = new File(result);
                                    if (file.exists()) {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                        Bitmap bitmap = BitmapFactory.decodeFile(result, options);
                                        img.setImageBitmap(bitmap);
                                        filename = result;
                                        tvNetworkTime.setText(strTime);
                                        tvNetworkTime.setTextColor(getResources().getColor(R.color.red));

                                        //persistLog(file.getPath(), strTime, "OUT");
                                    }
                                } else {
                                    CameraManager.getInstance().getCamera().startPreview();
                                }
                                capturing = false;
                            }

                            @Override
                            public void onObjectRequestError(String message) {
                                Log.e("Jeonsoft", "Error: " + message);
                                CameraManager.getInstance().getCamera().startPreview();
                            }

                            @Override
                            public void onRequestCancelled() {
                                CameraManager.getInstance().getCamera().startPreview();
                            }

                            @Override
                            public void onPreRequest() {
                            }
                        }, strTime, edt.getText().toString()).execute(bytes);
                    }
                });
            }
        });
        edt = (EditText) findViewById(R.id.edtEmployeeCode);
        edt.setHint("Enter your access code");
        edt.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 4) {
                    hideKeyboard();
                }
            }
        });
        //new TimerAsyncTask().execute();
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private final AbstractUploadServiceReceiver uploadReceiver = new AbstractUploadServiceReceiver() {
        @Override
        public void onProgress(String uploadId, int progress) {

        }

        @Override
        public void onError(String uploadId, Exception exception) {
            Log.e("Jeonsoft", "Error in upload with ID: " + uploadId + ". " + exception.getLocalizedMessage());
        }

        @Override
        public void onCompleted(String uploadId, int serverResponseCode, String serverResponseMessage, int totalItems, int totalUploaded, String tag, int total, int current) {
            Log.e("Jeonsoft", "Upload complete. " + serverResponseMessage);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        cfp.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cfp.onPause();
        uploadReceiver.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_capture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.switch_camera) {
            CameraManager.getInstance().switchCamera();
            cfp.onResume();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class SavePhotoTask extends AsyncTask<byte[], String ,String> {
        private ObjectCallback<Void, String> callback;
        private String fileId;
        private String code;

        public SavePhotoTask(ObjectCallback<Void, String> callback, String fileId, String code) {
            this.callback = callback;
            this.fileId = fileId;
            this.code = code;
        }

        @Override
        protected String doInBackground(byte[]... bytes) {
            String sdState = android.os.Environment.getExternalStorageState();
            File cacheDir;
            if (sdState.equals(Environment.MEDIA_MOUNTED)) {
                File sdDir = android.os.Environment.getExternalStorageDirectory();
                cacheDir = new File(sdDir, "data/jeonsoft");
            } else {
                cacheDir = getApplicationContext().getCacheDir();
            }

            if (!cacheDir.exists())
                cacheDir.mkdirs();

            try {
                File imageDir = new File(cacheDir.getPath() + "/employee_photos");
                if (!imageDir.exists())
                    imageDir.mkdirs();
                File photo = new File(imageDir.getPath(), code.trim().replace("/", "").replace(":", "").replace(" ", "")
                        + fileId.trim().replace("/", "").replace(":", "").replace(" ", "") + ".jpg");

                if (photo.exists()) {
                    photo.delete();
                }

                FileOutputStream fos = new FileOutputStream(photo.getPath());
                fos.write(bytes[0]);

                /*Bitmap bmp = ImageUtils.bitmapFromBytes(bytes[0]);
                Matrix mtx = new Matrix();
                mtx.setScale(-1, 1);
                mtx.postTranslate(bmp.getWidth(), 0);
                //mtx.postRotate(450);
                mtx.postRotate(-90);
                Bitmap scaled = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mtx, true);
                scaled.compress(Bitmap.CompressFormat.JPEG, 100, fos);*/

                fos.close();
                return photo.getPath();
            } catch (IOException ex) {
                callback.onObjectRequestError(ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            callback.onObjectRequestComplete(s);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callback.onPreRequest();
        }
    }
}
