package com.jeonsoft.facebundypro.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.jeonsoft.facebundypro.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wendell Wayne on 11/14/2014.
 */
public class CameraFacePreview extends SurfaceView implements SurfaceHolder.Callback {
    private static List<CameraFacePreview> all;
    private SurfaceHolder previewHolder = null;
    private Camera camera = null;
    private CameraFaceLayer faceDetectionlayer = new CameraFaceLayer(getWidth(), getHeight());

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //onTrainScreenDraw(canvas);

        //faceDetectionlayer.drawFaceDetection(canvas, getWidth(), getHeight());
    }
    private boolean hasFaceDetected = false;

    @SuppressLint("NewApi")
    Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            faceDetectionlayer.setFaces(faces);
            CameraFacePreview.this.invalidate();
            hasFaceDetected = faces.length > 0;
            if (faces.length == 0) {
                Logger.logE("No Face");
            } else {
                Logger.logE("Got a fucking face " + String.valueOf(faces.length));
            }
        }
    };

    public CameraFacePreview(Context context) {
        super(context);
        init();
    }

    public CameraFacePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraFacePreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private static void add(CameraFacePreview cp) {
        if (all == null) {
            all = new ArrayList<CameraFacePreview>();
        }

        if (!all.contains(cp)) {
            all.add(cp);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Point getScreenSize(Context mContext) {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            return size;
        } else {
            size.x = display.getWidth();
            size.y = display.getHeight();
            return size;
        }
    }

    private void remove(CameraFacePreview cp) {
        if (all != null) {
            if (all.contains(cp)) {
                all.remove(cp);
            }

            if (all.size() == 0) {
                CameraManager.getInstance().freeCamera();
                camera.release();
                camera = null;
            }
        }
    }

    public void init() {
        setWillNotDraw(false);
        Point screenSize = CameraFacePreview.getScreenSize(getContext());

        camera = CameraManager.getInstance().getCamera();
        camera.setFaceDetectionListener(faceDetectionListener);
        previewHolder = getHolder();
        previewHolder.addCallback(this);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        previewHolder.setFixedSize(screenSize.x, screenSize.y);
        add(this);
    }

    public void onResume() {
        init();
        surfaceCreated(previewHolder);
        if (camera != null) {
            camera.startPreview();
        }
    }

    public void onPause() {
        if (camera != null) {
            camera.stopPreview();
        }
    }

    private void onTrainScreenDraw(Canvas canvas) {
        CameraFaceLayer layer = new CameraFaceLayer(getWidth(), getHeight());
        if (hasFaceDetected) {
            layer.drawBorderPath(canvas);
            layer.drawCircleArc(canvas, 20);
        }
        //layer.drawTakenPhoto(getContext(), canvas, previewImagePath);
        //layer.drawFocusArea(canvas);
    }

    public void setPreviewImagePath(String path) {
        String previewImagePath = path;
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return (result);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera == null || holder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
            camera.setPreviewDisplay(holder);
            CameraManager.setCorrectCameraDisplayOrientation((Activity) getContext(),
                    CameraManager.getFacingCameraID(CameraManager.getInstance().getCameraFaceType()),
                    camera);
            camera.startFaceDetection();
            camera.startPreview();
        } catch (Throwable t) {
            Log.e("JEONSOFT", t.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
            if (e != null && e.getMessage() != null)
                Log.e("JEONSOFT", e.getMessage());
        }

        try {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height, parameters);

            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);

                /*@TODO: Bug - When switched to back camera, preview is rotated 90 degrees and when switched
                    and when switched back to front, preview is remains rotated.
                **/
                int rotation = CameraManager.getCorrectCameraRotation((Activity) getContext(),
                        CameraManager.getFacingCameraID(CameraManager.getInstance().getCameraFaceType()));

                if (rotation != -1)
                    parameters.setRotation(rotation);

                camera.setParameters(parameters);
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                camera.startFaceDetection();
            }
        } catch (Exception e) {
            Log.e("JEONSOFT", e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            camera.stopFaceDetection();
            camera.stopPreview();
            getHolder().removeCallback(this);
            camera.setPreviewCallback(null);
            camera.setPreviewDisplay(null);
            camera.setDisplayOrientation(0);
        } catch (Throwable t) {
            Log.e("JEONSOFT", t.getMessage());
        }
        remove(this);
    }
}
