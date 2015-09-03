package com.jeonsoft.facebundypro.views;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.gc.materialdesign.widgets.Dialog;
import com.jeonsoft.facebundypro.DialogFragmentResultListener;
import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.SelectedFaceOptionsDialogFragment;
import com.jeonsoft.facebundypro.data.Subject;
import com.jeonsoft.facebundypro.data.SubjectsDataSource;
import com.jeonsoft.facebundypro.net.ConnectivityHelper;
import com.jeonsoft.facebundypro.net.ReachableServerHost;
import com.jeonsoft.facebundypro.net.ReachableServerHostListener;
import com.jeonsoft.facebundypro.net.ServerHostStatus;
import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.jeonsoft.facebundypro.uploadservice.SubjectDownloadService;
import com.jeonsoft.facebundypro.uploadservice.SubjectMultiPartUploadService;
import com.jeonsoft.facebundypro.uploadservice.SubjectUploader;
import com.jeonsoft.facebundypro.widgets.Style;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WendellWayne on 3/14/2015.
 */
public class SubjectManager extends BaseActionBarActivity {
    //private ListView listView;
    private GridView gridView;
    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateAdapter();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_face_manager_grid);
        //listView = (ListView) findViewById(R.id.lvFaceTemplates);
        gridView = (GridView) findViewById(R.id.gvFaceTemplates);
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                FaceTemplateListItem subject = (FaceTemplateListItem) parent.getItemAtPosition(position);
                final String code = subject.accessCode;
                Bundle bundle = new Bundle();
                DialogFragment sfd = SelectedFaceOptionsDialogFragment.newInstance(code, bundle, new DialogFragmentResultListener() {
                    @Override
                    public void onResultReturned(Object value) {
                        if ((int) value == 0)
                            deleteFace(code);
                    }

                    @Override
                    public void onResultWithValueReturned(boolean valid, Object value) {

                    }
                });
                sfd.show(getSupportFragmentManager(), "selected_face_options");
                return true;
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    private void clearTemplates()  {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                SubjectsDataSource ds = SubjectsDataSource.getInstance(SubjectManager.this);
                try {
                    ds.open();
                    ds.clear();
                    LogsReportActivity.deleteFiles(GlobalConstants.getEmployeeFaceTemplatesDir(SubjectManager.this));
                } catch(Exception ex) {
                    logError("Error deleting face templates: " + ex.getMessage());
                } finally {
                    ds.close();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                updateAdapter();
            }
        }.execute();
    }

    private void deleteFace(final String accessCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SubjectsDataSource ds = SubjectsDataSource.getInstance(SubjectManager.this);
                try {
                    if (!ds.isOpen())
                        ds.open();
                    ds.deleteByAccessCode(accessCode);

                    File fileTemplate = new File(GlobalConstants.getEmployeeFaceTemplatesDir(SubjectManager.this).concat("/").concat(accessCode).concat(".dat"));
                    File filePhoto = new File(GlobalConstants.getEmployeeFaceTemplatesDir(SubjectManager.this).concat("/").concat(accessCode).concat(".jpg"));

                    if (fileTemplate.exists())
                        fileTemplate.delete();
                    if (filePhoto.exists())
                        filePhoto.delete();

                } catch (Exception ex) {
                    logError(ex.getMessage());
                    showCrouton(ex.getMessage(), Style.ALERT);
                } finally {
                    if (ds.isOpen())
                        ds.close();
                    updateAdapter();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_face_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_synch) {
            if (ConnectivityHelper.isConnected(getApplicationContext())) {
                synchTemplates();
                return true;
            } else {
                showSnackBar("You need an internet connection to sync face templates.", "", null);
                return false;
            }
        } else if (item.getItemId() == R.id.action_clear) {
            final Dialog dialog = new com.gc.materialdesign.widgets.Dialog(this, "", "Are you sure you want to clear face templates?");
            dialog.setOnCancelButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTemplates();
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

    private SubjectUploader uploader;

    private void synchTemplates() {
        //uploadTemplates();
        //downloadTemplates();

        if(ConnectivityHelper.isConnected(this)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] hosts = CacheManager.getInstance(SubjectManager.this).getStringPreference(CacheManager.SERVER_HOSTS).split("\n"); //new String[] {"http://10.0.0.82:3003", "http://activation.facebundy.com"};
                    new ReachableServerHost(SubjectManager.this, hosts, new ReachableServerHostListener() {
                        @Override
                        public void onStatusChanged(ServerHostStatus status, String host) {
                            logDebug(status.toString() + ": " + host);
                        }

                        @Override
                        public void onReachableHostAcquired(String reachableHost) {
                            try {
                                Intent intent = new Intent(SubjectManager.this, SubjectMultiPartUploadService.class);
                                intent.putExtra("URL", reachableHost);
                                SubjectManager.this.startService(intent);
                            } catch(Exception ex) {
                                logError(ex.getMessage());
                            } finally {

                            }
                        }

                        @Override
                        public void onFailedHostAcquisition(String message) {
                            showSnackBar(message, "Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    synchTemplates();
                                }
                            });
                        }
                    }).execute();
                }
            });
        } else {
            showSnackBar("Please connect to the internet to sync face templates.", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubjectManager.this.synchTemplates();
                }
            });
        }
    }

    /*private void uploadTemplates() {
        if(ConnectivityHelper.isConnected(this)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] hosts = CacheManager.getInstance(SubjectManager.this).getStringPreference(CacheManager.SERVER_HOSTS).split("\n"); //new String[] {"http://10.0.0.82:3003", "http://activation.facebundy.com"};
                    new ReachableServerHost(SubjectManager.this, hosts, new ReachableServerHostListener() {
                        @Override
                        public void onStatusChanged(ServerHostStatus status, String host) {
                            logDebug(status.toString() + ": " + host);
                        }

                        @Override
                        public void onReachableHostAcquired(String reachableHost) {
                            SubjectsDataSource ds = SubjectsDataSource.getInstance(SubjectManager.this);
                            try {
                                ds.open();
                                List<Subject> subjects = ds.getAllSubjects();
                                uploader = SubjectUploader.getInstance(SubjectManager.this);
                                uploader.upload(subjects, reachableHost);
                            } catch(Exception ex) {
                                logError(ex.getMessage());
                            } finally {
                                ds.close();
                            }
                        }

                        @Override
                        public void onFailedHostAcquisition(String message) {
                            showSnackBar(message, "Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    uploadTemplates();
                                }
                            });
                        }
                    }).execute();
                }
            });
        } else {
            showSnackBar("Please connect to the internet to sync face templates.", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubjectManager.this.downloadTemplates();
                }
            });
        }
    }

    private void downloadTemplates() {
        if(ConnectivityHelper.isConnected(this)) {
            final Intent intent = new Intent(SubjectManager.this, SubjectDownloadService.class);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] hosts = CacheManager.getInstance(SubjectManager.this).getStringPreference(CacheManager.SERVER_HOSTS).split("\n"); //new String[] {"http://10.0.0.82:3003", "http://activation.facebundy.com"};
                    new ReachableServerHost(SubjectManager.this, hosts, new ReachableServerHostListener() {
                        @Override
                        public void onStatusChanged(ServerHostStatus status, String host) {
                            logDebug(status.toString() + ": " + host);
                        }

                        @Override
                        public void onReachableHostAcquired(String reachableHost) {
                            intent.putExtra("URL", reachableHost);
                            SubjectManager.this.startService(intent);
                        }

                        @Override
                        public void onFailedHostAcquisition(String message) {
                            showSnackBar(message, "Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    downloadTemplates();
                                }
                            });
                        }
                    }).execute();
                }
            });
        } else {
            showSnackBar("Please connect to the internet to sync face templates.", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubjectManager.this.downloadTemplates();
                }
            });
        }
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        if (uploader != null) {
            try {
                uploader.unregister();
            } catch (Exception ex){}
        }

        unregisterReceiver(downloadReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (uploader != null) {
            try {
                uploader.register();
            } catch(Exception ex) {}
        }

        registerReceiver(downloadReceiver, new IntentFilter(SubjectMultiPartUploadService.TAG));
    }

    private void init() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                /*.memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                .diskCacheExtraOptions(480, 800, null)
                .taskExecutor(...)
                .taskExecutorForCachedImages(...)
                .threadPoolSize(3) // default
                .threadPriority(Thread.NORM_PRIORITY - 2) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .diskCache(new UnlimitedDiscCache(cacheDir)) // default
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .imageDownloader(new BaseImageDownloader(context)) // default
                .imageDecoder(new BaseImageDecoder()) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .writeDebugLogs()*/
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);

        SubjectsDataSource ds = SubjectsDataSource.getInstance(this);
        try {
            ds.open();
            List<Subject> subjects = ds.getAllSubjects();
            ArrayList<FaceTemplateListItem> items = new ArrayList<>();
            for (int i = 0; i < subjects.size(); i++) {
                Subject subject = subjects.get(i);
                FaceTemplateListItem item = new FaceTemplateListItem(subject.getId(), subject.getAccessCode(), subject.getTemplate());
                items.add(item);
            }

            FaceTemplateListAdapter adapter = new FaceTemplateListAdapter(this, R.layout.face_template_grid_item, items); //@TODO: R.layout.face_template_list_item -> for listview
            gridView.setAdapter(adapter);
            TextView textView = (TextView) findViewById(R.id.tvEnrolledFaces);
            textView.setText(String.format("Enrolled Faces (%d)", subjects.size()));
            //listView.setAdapter(adapter);
        } catch (Exception ex) {
            logError(ex.getMessage());
        } finally {
           if (ds != null)
               ds.close();
        }
    }

    private void updateAdapter() {
        ImageLoader.getInstance().clearDiskCache();
        ImageLoader.getInstance().clearMemoryCache();

        FaceTemplateListAdapter adapter = null;
        if (gridView != null)
            adapter = (FaceTemplateListAdapter) gridView.getAdapter();
        if (adapter != null) {
            SubjectsDataSource ds = SubjectsDataSource.getInstance(this);
            try {
                if (!ds.isOpen())
                    ds.open();
                List<Subject> subjects = ds.getAllSubjects();
                ArrayList<FaceTemplateListItem> items = new ArrayList<>();
                for (int i = 0; i < subjects.size(); i++) {
                    Subject subject = subjects.get(i);
                    FaceTemplateListItem item = new FaceTemplateListItem(subject.getId(), subject.getAccessCode(), subject.getTemplate());
                    items.add(item);
                }

                adapter.setData(items);
                //gridView.invalidateViews();

                TextView textView = (TextView) findViewById(R.id.tvEnrolledFaces);
                textView.setText(String.format("Enrolled Faces (%d)", subjects.size()));
            } catch (Exception ex) {
                logError(ex.getMessage());
            } finally {
                if (ds != null) {
                    try {
                        if (ds.isOpen())
                            ds.close();
                    } catch(Exception ex) {}
                }
            }
        }
    }

    class FaceTemplateListAdapter extends ArrayAdapter<FaceTemplateListItem> {
        private Context context;
        private int layoutResourceId;
        private ArrayList<FaceTemplateListItem> data;

        public FaceTemplateListAdapter(Context context, int layoutResourceId, ArrayList<FaceTemplateListItem> data) {
            super(context, layoutResourceId, data);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.data = data;
        }

        public void setData(ArrayList<FaceTemplateListItem> data) {
            this.data = data;
            this.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            if (data.size() > 0) {
                final FaceTemplateListItem item = data.get(position);

                TextView accessCode = (TextView) convertView.findViewById(R.id.tvAccessCode);
                accessCode.setTag(item.id);
                accessCode.setText(item.accessCode);

                final CircleImageView image = (CircleImageView) convertView.findViewById(R.id.imgFaceImage);
                image.setTag(item.id);

                CacheManager cm = CacheManager.getInstance(context);
                int edition = cm.getIntPreference(CacheManager.EDITION);
                String path = GlobalConstants.getEmployeePhotosDirectory(SubjectManager.this);
                if (edition == 3)
                    path = GlobalConstants.getEmployeeFaceTemplatesDir(SubjectManager.this);
                File file = new File(path + "/" + accessCode.getText().toString().trim() + ".jpg");
                if (!file.exists())
                    image.setImageDrawable(SubjectManager.this.getResources().getDrawable(R.drawable.face_bundy_logo));
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
        public FaceTemplateListItem getItem(int position) {
            return data.get(position);
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

    private class FaceTemplateListItem {
        private String accessCode;
        private byte[] template;
        private int id;

        public FaceTemplateListItem(int id, String accessCode, byte[] template) {
            this.accessCode = accessCode;
            this.id = id;
            this.template = template;
        }
    }
}
