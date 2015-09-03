package com.jeonsoft.facebundypro.biometrics.licensing;

import android.content.Context;
import android.os.AsyncTask;

import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.ConnectivityHelper;
import com.jeonsoft.facebundypro.utils.ObjectCallback;

import java.util.List;

/**
 * Created by WendellWayne on 3/3/2015.
 */
public final class LicenseActivator {
    private enum Task {
        ACTIVATE, DEACTIVATE
    }
    private Context context;
    private boolean isModified = false;
    private BackgroundTask mBackgroundTask = null;
    private ActivationCallback callback;

    public void setActivationCallback(ActivationCallback callback) {
        this.callback = callback;
    }

    public LicenseActivator(Context context) {
        this.context = context;
    }

    public List<License> getLicenses() {
        List<License> licenses = LicensingServiceManager.getLicenses();
        return licenses;
    }

    public void activate() {
        try {
            if (ConnectivityHelper.isConnected(context)) {
                if (mBackgroundTask == null) {
                    mBackgroundTask = new BackgroundTask(new ObjectCallback<String, ActivationCallbackArgs>() {
                        @Override
                        public ActivationCallbackArgs onObjectRequestProcess(String... params) {
                            return null;
                        }

                        @Override
                        public void onObjectRequestComplete(ActivationCallbackArgs result) {
                            if (result.task == Task.ACTIVATE)
                                callback.onActivate(result.success);
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
                    });
                    mBackgroundTask.activate(getLicenses());
                }
            } else {
                Logger.logE("Unable to activate license. No internet connection.");
            }
        } catch (Exception e) {
            Logger.logE(e.getMessage());
        }
    }

    public void deactivate() {
        try {
            if (ConnectivityHelper.isConnected(context)) {
                if (mBackgroundTask == null) {
                    mBackgroundTask = new BackgroundTask(new ObjectCallback<String, ActivationCallbackArgs>() {
                        @Override
                        public ActivationCallbackArgs onObjectRequestProcess(String... params) {
                            return null;
                        }

                        @Override
                        public void onObjectRequestComplete(ActivationCallbackArgs result) {
                            if (result.task == Task.DEACTIVATE)
                                callback.onDeactivate(result.success);
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
                    });
                    mBackgroundTask.deactivate(getLicenses());
                }
            } else {
                Logger.logE("Unable to deactivate license. No internet connection.");
            }
        } catch (Exception e) {
            Logger.logE(e.getMessage());
        }
    }

    class ActivationCallbackArgs {
        public Task task;
        public boolean success;

        public ActivationCallbackArgs(Task task, boolean success) {
            this.task = task;
            this.success = success;
        }
    }
    private final class BackgroundTask extends AsyncTask<Boolean, String, String> {
        private ObjectCallback<String, ActivationCallbackArgs> callback;

        private Task mTask;
        private List<License> mLicenses;

        public BackgroundTask(ObjectCallback<String, ActivationCallbackArgs> callback) {
            this.callback = callback;
        }
        void activate(List<License> licenses) {
            if (licenses == null) throw new NullPointerException("licenses");
            if (licenses.isEmpty()) throw new IllegalArgumentException("licenses < 0");
            mTask = Task.ACTIVATE;
            mLicenses = licenses;
            execute();
        }

        void deactivate(List<License> licenses) {
            if (licenses == null) throw new NullPointerException("licenses");
            if (licenses.isEmpty()) throw new IllegalArgumentException("licenses < 0");
            mTask = Task.DEACTIVATE;
            mLicenses = licenses;
            execute();
        }

        @Override
        protected String doInBackground(Boolean... params) {
            boolean hasInternet = ConnectivityHelper.isConnected(context);
            try {
                if (!isCancelled()) {
                    switch (mTask) {
                        case ACTIVATE:
                            if (hasInternet) {
                                isModified = true;
                            }
                            for (License license : mLicenses) {
                                if (!license.isActivated())
                                    license.activate(hasInternet);
                            }
                            return hasInternet ? "Activation succeeded." : "Proceed activation online.";
                        case DEACTIVATE:
                            if (hasInternet) {
                                isModified = true;
                            }
                            for (License license : mLicenses) {
                                if (license.isActivated())
                                    license.deactivate(hasInternet);
                            }
                            return hasInternet ? "Deactivation succeeded." : "Proceed deactivation online.";
                    }
                }
            } catch (Exception e) {
                Logger.logE(e.getMessage());
                return e.getMessage();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... messages) {
            for (String message : messages) {
                Logger.logI(message);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Task task;
            boolean activated;

            if (result.equals("Activation succeeded.")) {
                task = Task.ACTIVATE;
                activated = true;
            } else if (result.equals("Proceed activation online.")) {
                task = Task.ACTIVATE;
                activated = false;
            } else if (result.equals("Deactivation succeeded.")) {
                task = Task.DEACTIVATE;
                activated = true;
            } else {
                task = Task.DEACTIVATE;
                activated = false;
            }
            callback.onObjectRequestComplete(new ActivationCallbackArgs(task, activated));
        }
    }
}
