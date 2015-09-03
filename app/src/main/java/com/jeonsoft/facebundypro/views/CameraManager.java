package com.jeonsoft.facebundypro.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

/**
 * Created by Wendell Wayne on 11/14/2014.
 */
public class CameraManager {
    private static CameraManager instance;

    private boolean isFrontCamera = true;
    private Camera camera = null;

    private CameraManager() {
        freeCamera();
        camera = openFrontCamera();
    }

    public static CameraManager getInstance() {
        if (instance == null)
            instance = new CameraManager();
        if (instance.camera == null) {
            if (instance.isFrontCamera())
                instance.camera = instance.openFrontCamera();
            else
                instance.camera = instance.openBackCamera();
        }
        return instance;
    }

    public void freeCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void switchCamera() {
        freeCamera();
        if (isFrontCamera()) {
            camera = openBackCamera();
        } else {
            camera = openFrontCamera();
        }
    }

    public Camera getCamera() {
        return camera;
    }

    public boolean isFrontCamera() {
        return isFrontCamera;
    }

    public int getCameraFaceType() {
        return isFrontCamera() ? Camera.CameraInfo.CAMERA_FACING_FRONT: Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    @SuppressLint("NewApi")
    private Camera openBackCamera() {
        Camera cam = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            try {
                cam = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                isFrontCamera = false;
                return cam;
            } catch (Exception e) {
                isFrontCamera = true;
                Log.e("JEONSOFT", e.getMessage());
            }

        } else {
            try {
                cam = Camera.open();
                Camera.CameraInfo camInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(0, camInfo);
                if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    isFrontCamera = false;
                } else {
                    isFrontCamera = true;
                }
                return cam;
            } catch (Exception e) {
                Log.e("JEONSOFT", e.getMessage());
            }
        }

        return cam;
    }

    public static int getFacingCameraID(int face) {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == face) {
                Log.d("Jeonsoft", "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public static void setCorrectCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public static int getCorrectCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static int getCorrectCameraRotation(Activity activity, int cameraId) {
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_UNDEFINED) return -1;
        orientation = 1;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        orientation = (orientation + 45) / 90 * 90;
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {  // back-facing camera
            rotation = (info.orientation + orientation) % 360;
        }
        return rotation;
    }


    @SuppressLint("NewApi")
    private Camera openFrontCamera() {
        Camera cam = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            try {
                cam = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                isFrontCamera = true;
                return cam;
            } catch (Exception e) {
                isFrontCamera = false;
                Log.e("JEONSOFT", e.getMessage());
            }

        } else {
            try {
                cam = Camera.open();
                Camera.CameraInfo camInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(0, camInfo);
                if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    isFrontCamera = true;
                } else {
                    isFrontCamera = false;
                }
                return cam;
            } catch (Exception e) {
                Log.e("JEONSOFT", e.getMessage());
            }
        }

        return cam;
    }
}
