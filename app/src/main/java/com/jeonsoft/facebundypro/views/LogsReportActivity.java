package com.jeonsoft.facebundypro.views;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gc.materialdesign.views.ProgressBarIndeterminate;
import com.gc.materialdesign.widgets.Dialog;
import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.TimeLogUploadHelper;
import com.jeonsoft.facebundypro.data.TimeLog;
import com.jeonsoft.facebundypro.data.TimelogDataSource;
import com.jeonsoft.facebundypro.licensing.AppEditions;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.ConnectivityHelper;
import com.jeonsoft.facebundypro.net.ReachableServerHost;
import com.jeonsoft.facebundypro.net.ReachableServerHostListener;
import com.jeonsoft.facebundypro.net.ServerHostStatus;
import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.jeonsoft.facebundypro.uploadservice.AbstractUploadServiceReceiver;
import com.jeonsoft.facebundypro.widgets.Style;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by WendellWayne on 2/12/2015.
 */
public class LogsReportActivity extends BaseActionBarActivity {
    private LogsListAdapter adapter;
    private ListView lsv;

    private final AbstractUploadServiceReceiver uploadReceiver = new AbstractUploadServiceReceiver() {
        @Override
        public void onProgress(String uploadId, int progress) {
            progressBar.setVisibility(View.VISIBLE);
            MenuItem mnuUpload = menu.findItem(R.id.action_upload);
            MenuItem mnuDelete = menu.findItem(R.id.action_clear);
            mnuUpload.setVisible(false);
            mnuDelete.setVisible(false);
            Logger.logI("The progress of the upload with ID " + uploadId + " is: " + progress);
        }

        @Override
        public void onError(String uploadId, Exception exception) {
            String message = "Error in upload with ID: " + uploadId + ". " + exception.getLocalizedMessage();
            progressBar.setVisibility(View.GONE);
            MenuItem mnuUpload = menu.findItem(R.id.action_upload);
            MenuItem mnuDelete = menu.findItem(R.id.action_clear);
            mnuUpload.setVisible(true);
            mnuDelete.setVisible(true);
            Logger.logE(message);
        }

        @Override
        public void onCompleted(String uploadId, int serverResponseCode, String serverResponseMessage, int totalItemsToUpload, int totalItemsUploaded, String tag, int total, int current) {
            String message = "Upload with ID " + uploadId + " is completed: " + serverResponseCode + ", "
                    + serverResponseMessage;
            Logger.logI(message);
            if (current >= total) {
                progressBar.setVisibility(View.GONE);
                MenuItem mnuUpload = menu.findItem(R.id.action_upload);
                MenuItem mnuDelete = menu.findItem(R.id.action_clear);
                mnuUpload.setVisible(true);
                mnuDelete.setVisible(true);
                Logger.logE("COMPLETED.");
                LogsReportActivity.this.adapter.clear();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        TimeLogUploadHelper helper = TimeLogUploadHelper.getInstance(this);
        try {
            //helper.register();
            uploadReceiver.register(LogsReportActivity.this);
        } catch(Exception ex) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        TimeLogUploadHelper helper = TimeLogUploadHelper.getInstance(this);
        try {
            //helper.unregister();
            uploadReceiver.unregister(LogsReportActivity.this);
        } catch(Exception ex) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logs_list);

        init();
    }

    private Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_time_logs, menu);
        this.menu = menu;
        boolean all = getIntent().getBooleanExtra("ALL", true);

        if (!all) {
            MenuItem mnuUploadLogs = menu.getItem(1);
            MenuItem mnuDeleteLogs = menu.getItem(2);
            mnuUploadLogs.setVisible(false);
            mnuDeleteLogs.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isDateSynched() {
        CacheManager cm = CacheManager.getInstance(this);
        if (cm.containsPreference(CacheManager.SERVER_TIME) && cm.containsPreference(CacheManager.ELAPSED_TIME)) {
            long elapsed = cm.getLongPreference(CacheManager.ELAPSED_TIME);
            if (elapsed >= SystemClock.elapsedRealtime()) {
                showSnackBar("FaceBundy time is out of sync!", "Sync", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadTimeLogs();
                    }
                });
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_upload) {
            if (isDateSynched()) {
                uploadTimeLogs();
                return true;
            }
            return false;
        } else if (item.getItemId() == R.id.action_clear) {
            final Dialog dialog = new Dialog(this, "", "Are you sure you want to delete all time logs?");
            dialog.setOnCancelButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTimelogs();
                }
            });
            dialog.addCancelButton("Cancel");
            if (dialog.getButtonAccept() != null)
                dialog.getButtonAccept().setText("Delete");
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearTimelogs() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TimelogDataSource ds = TimelogDataSource.getInstance(LogsReportActivity.this);
                try {
                    ds.open();
                    ds.clearTimeLogs();
                    deleteFiles(GlobalConstants.getEmployeePhotosDirectory(LogsReportActivity.this));
                    showSnackBar("All time logs deleted.", "OK", null);
                    adapter.clear();
                } catch (SQLException ex) {
                    logError(ex.getMessage());
                } finally {
                    ds.close();
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


    private void uploadTimeLogs() {
        if(ConnectivityHelper.isConnected(this)) {
            final TimeLogUploadHelper helper = TimeLogUploadHelper.getInstance(this);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] hosts = CacheManager.getInstance(LogsReportActivity.this).getStringPreference(CacheManager.SERVER_HOSTS).split("\n"); //new String[] {"http://10.0.0.82:3003", "http://activation.facebundy.com"};
                    new ReachableServerHost(LogsReportActivity.this, hosts, new ReachableServerHostListener() {
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
                            showSnackBar(message, "Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    uploadTimeLogs();
                                }
                            });
                        }
                    }).execute();
                }
            });
        } else {
            showSnackBar("Please connect to the internet to upload time logs.", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogsReportActivity.this.uploadTimeLogs();
                }
            });
        }
    }

    private ProgressBarIndeterminate progressBar;

    private void init() {
        lsv = (ListView) findViewById(R.id.lvTimeLogs);
        progressBar = (ProgressBarIndeterminate) findViewById(R.id.progressBarIndeterminate);
        String accessCode = getIntent().getStringExtra("ACCESS_CODE");
        boolean all = getIntent().getBooleanExtra("ALL", true);
        logDebug("Filter: " + accessCode);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
        TimelogDataSource ds = TimelogDataSource.getInstance(this);
        try {
            ds.open();
            List<TimeLog> logs;
            if (accessCode == "" || accessCode.isEmpty() || all)
                logs = ds.getAllTimeLogs();
            else
                logs = ds.getAllTimeLogsByAccessCode(accessCode);

            ArrayList<LogListItem> items = getLogListItems(logs);
            adapter = new LogsListAdapter(this, R.layout.log_list_item, items);
            lsv.setAdapter(adapter);
            View view = getLayoutInflater().inflate(R.layout.empty_view, null);
            lsv.setEmptyView(view);
            ds.close();
        } catch (Exception ex) {
            showCrouton(ex.getMessage() != null ? ex.getMessage() : ex.toString(), Style.ALERT);
        } finally {
            if (ds != null)
                ds.close();
            progressBar.setVisibility(View.GONE);
        }
    }

    private ArrayList<LogListItem> getLogListItems(List<TimeLog> logs) {
        ArrayList<LogListItem> items = new ArrayList<LogListItem>();
        for (int i = 0; i < logs.size(); i++) {
            TimeLog log = logs.get(i);
            String location = "at unknown location.";
            if (log.getGpsLatitude() != 0 && log.getGpsLongitude() != 0)
                location = "near " + String.valueOf(log.getGpsLatitude()) + ", " + String.valueOf(log.getGpsLongitude());
            items.add(new LogListItem(i, log.getAccessCode(), log.getTime(), log.getType(), location, log.getFilename()));
        }
        return items;
    }

    class LogsListAdapter extends ArrayAdapter<LogListItem> {
        private Context context;
        private int layoutResourceId;
        private ArrayList<LogListItem> data;

        public LogsListAdapter(Context context, int layoutResourceId, ArrayList<LogListItem> data) {
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
            if(data.size() > 0) {
                LogListItem item = data.get(position);

                TextView accessCode = (TextView) convertView.findViewById(R.id.tvAccessCode);
                accessCode.setTag(item.id);
                accessCode.setText(item.accessCode);

                TextView time = (TextView) convertView.findViewById(R.id.tvTime);
                time.setTag(item.id);
                time.setText(item.time);

                TextView type = (TextView) convertView.findViewById(R.id.tvType);
                type.setTag(item.id);
                type.setText(item.type);

                TextView location = (TextView) convertView.findViewById(R.id.tvLocation);
                location.setTag(item.id);
                location.setText(item.location);

                TextView llStatus = (TextView) convertView.findViewById(R.id.llStatus);
                llStatus.setBackgroundColor(getResources().getColor(item.type.equals("IN") ? R.color.green : R.color.red));

                final CircleImageView image = (CircleImageView) convertView.findViewById(R.id.imgFaceImage);

                AppEditions appEdition = GlobalConstants.getInstance().getAppEdition();
                String path;
                File file = new File(item.fileName);
                if (appEdition == AppEditions.ExtractionAndMatching) {
                    path = GlobalConstants.getEmployeeFaceTemplatesDir(LogsReportActivity.this);
                    file = new File(path + "/" + accessCode.getText().toString().trim() + ".jpg");
                }
                if (!file.exists())
                    image.setImageDrawable(LogsReportActivity.this.getResources().getDrawable(R.drawable.face_bundy_logo));
                else {
                    ImageLoader.getInstance().displayImage("file:///" + Uri.fromFile(file).getPath(), image);
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
    }

    class LogListItem {
        public int id;
        public String accessCode;
        public String time;
        public String type;
        public String location;
        public String fileName;

        public LogListItem(int id, String accessCode, String time, String type, String location, String fileName) {
            this.id = id;
            this.accessCode = accessCode;
            this.time = time;
            this.type = type;
            this.location = location;
            this.fileName = fileName;
        }
    }
}
