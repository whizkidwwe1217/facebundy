package com.jeonsoft.facebundypro.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.widgets.Dialog;
import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.data.ItineraryProject;
import com.jeonsoft.facebundypro.data.ItineraryProjectDataSource;
import com.jeonsoft.facebundypro.data.ItineraryProjectTaskDataSource;
import com.jeonsoft.facebundypro.location.GPSTracker;
import com.jeonsoft.facebundypro.net.ConnectivityHelper;
import com.jeonsoft.facebundypro.net.ReachableServerHost;
import com.jeonsoft.facebundypro.net.ReachableServerHostListener;
import com.jeonsoft.facebundypro.net.ServerHostStatus;
import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.uploadservice.ItinerariesUploadService;
import com.jeonsoft.facebundypro.utils.ocpsoft.PrettyTime;
import com.jeonsoft.facebundypro.widgets.PinnedSectionListView;
import com.jeonsoft.facebundypro.widgets.Style;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by WendellWayne on 6/25/2015.
 */
public class ItineraryProjectListActivity extends BaseActionBarActivity {
    private PinnedSectionListView lv;
    private ItineraryAdapter adapter;
    private ButtonFloat btnNewLog;
    private String accessCode;
    private boolean allEmployees = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            accessCode = savedInstanceState.getString("ACCESS_CODE");
            allEmployees = savedInstanceState.getBoolean("ALL_EMPLOYEES");
        } else if (getIntent() != null && getIntent().hasExtra("ACCESS_CODE")) {
            accessCode = getIntent().getStringExtra("ACCESS_CODE");
            if (getIntent().hasExtra("ALL_EMPLOYEES"))
                allEmployees = true;
        } else
            accessCode = "empty";
        logError("Create " + accessCode);

        setContentView(R.layout.activity_itinerary_list);
        init();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("ACCESS_CODE", accessCode);
        outState.putBoolean("ALL_EMPLOYEES", allEmployees);
        logError("Saved state " + accessCode);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        logError("Restored");
        accessCode = savedInstanceState.getString("ACCESS_CODE");
        allEmployees = savedInstanceState.getBoolean("ALL_EMPLOYEES");
    }

    @Override
    protected void onResume() {
        super.onResume();
        logError("Resume");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear) {
            clear();
            return true;
        } else if (item.getItemId() == R.id.action_upload) {
            logError("Uploading..");
            uploadItineraries();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadItineraries() {
        if(ConnectivityHelper.isConnected(this)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] hosts = CacheManager.getInstance(ItineraryProjectListActivity.this).getStringPreference(CacheManager.SERVER_HOSTS).split("\n"); //new String[] {"http://10.0.0.82:3003", "http://activation.facebundy.com"};
                    new ReachableServerHost(ItineraryProjectListActivity.this, hosts, new ReachableServerHostListener() {
                        @Override
                        public void onStatusChanged(ServerHostStatus status, String host) {
                            logDebug(status.toString() + ": " + host);
                        }

                        @Override
                        public void onReachableHostAcquired(String reachableHost) {
                            Intent intent = new Intent(ItineraryProjectListActivity.this, ItinerariesUploadService.class);
                            intent.putExtra("URL", reachableHost);
                            startService(intent);
                        }

                        @Override
                        public void onFailedHostAcquisition(String message) {
                            showSnackBar(message, "Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    uploadItineraries();
                                }
                            });
                        }
                    }).execute();
                }
            });
        } else {
            showSnackBar("Please connect to the internet to upload itineraries.", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItineraryProjectListActivity.this.uploadItineraries();
                }
            });
        }
    }

    public void clear() {
        final Dialog dialog = new Dialog(this, "Clear Itineraries", "Are you sure you want to delete all itineraries?");
        dialog.setOnCancelButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItineraryProjectDataSource ds = ItineraryProjectDataSource.getInstance(ItineraryProjectListActivity.this);
                try {
                    ds.open();
                    ds.beginTransaction();
                    if (allEmployees)
                        ds.clearItinerary();
                    else
                        ds.clearItineraryByAccessCode(accessCode);
                    ds.setTransactionSuccessful();
                } catch (Exception ex) {
                    logError(ex.getMessage());
                } finally {
                    if (ds != null && ds.isOpen()) {
                        ds.endTransaction();
                        ds.close();
                    }
                }

                /*ItineraryProjectTaskDataSource tsd = ItineraryProjectTaskDataSource.getInstance(ItineraryProjectListActivity.this);
                try {
                    tsd.open();
                    tsd.beginTransaction();
                    tsd.clearItineraryTask();
                    tsd.setTransactionSuccessful();
                } catch (Exception ex) {
                    logError(ex.getMessage());
                } finally {
                    if (tsd != null && tsd.isOpen()) {
                        tsd.endTransaction();
                        tsd.close();
                    }
                }*/
                adapter.setItems(new ArrayList<ItineraryProject>());
                adapter.notifyDataSetChanged();
            }
        });
        dialog.addCancelButton("Cancel");
        if (dialog.getButtonAccept() != null)
            dialog.getButtonAccept().setText("Delete");
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_itinerary_list, menu);

        MenuItem mnuEmployees = menu.findItem(R.id.action_employee_list);
        View view = mnuEmployees.getActionView();
        if (view instanceof Spinner) {
            final Spinner spinner = (Spinner) view;

            try {
                List<String> dateFilterList = new ArrayList<>();
                dateFilterList.add("All");
                dateFilterList.add("Day");
                dateFilterList.add("Week");
                dateFilterList.add("Month");
                dateFilterList.add("Date");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, dateFilterList);
                spinner.setAdapter(adapter);
            } catch(Exception ex) {
                logError("Error loading date filter: " + ex.getMessage());
            } finally {

            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void updateAdapter() {
        ItineraryProjectDataSource ds = ItineraryProjectDataSource.getInstance(this);
        List<ItineraryProject> items = new ArrayList<>();
        TreeSet<Integer> headers = new TreeSet<>();
        List<ItineraryProject> sectionedItems = new ArrayList<ItineraryProject>();

        try {
            ds.open();
            items = allEmployees ? ds.getAllItinerary() : ds.getAllItineraryByAccessCode(accessCode);
            List<String> dates = new ArrayList<String>();
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            SimpleDateFormat df2 = new SimpleDateFormat("MMM dd, yyyy");
            for(int i = 0; i < items.size(); i++) {
                ItineraryProject proj = items.get(i);
                Date date = df.parse(proj.timeIn);
                String d = df2.format(date);
                if (!dates.contains(d)) {
                    dates.add(d);
                    headers.add(i+headers.size());
                    ItineraryProject p = new ItineraryProject(0, "", d, "", "", 0, 0, "");
                    sectionedItems.add(p);
                }
                sectionedItems.add(proj);
            }

            adapter.setItems(sectionedItems);
            adapter.setHeaders(headers);
        } catch (Exception ex) {
            logError("Error loading itineraries: " + ex.getMessage());
        } finally {
            if (ds != null && ds.isOpen()) {
                ds.close();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CODE_NEW || resultCode == RESULT_CODE_EDIT &&
                requestCode == REQUEST_CODE_NEW || requestCode == REQUEST_CODE_EDIT) {
            updateAdapter();
        }
    }

    private double latitude, longitude;

    private void tagGPSLocation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GPSTracker gps = new GPSTracker(ItineraryProjectListActivity.this);
                if (gps.canGetLocation()) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                }
            }
        });
    }

    private void init() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);

        btnNewLog = (ButtonFloat) findViewById(R.id.btnNewLog);
        btnNewLog.setBackgroundColor(getResources().getColor(R.color.accent_color));
        btnNewLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagGPSLocation();
                Intent intent = new Intent(ItineraryProjectListActivity.this, ItineraryProjectItemActivity.class);
                intent.putExtra("ACCESS_CODE", accessCode);
                intent.putExtra("LATITUDE", latitude);
                intent.putExtra("LONGITUDE", longitude);
                intent.putExtra("MANUAL", true);
                startActivityForResult(intent, REQUEST_CODE_NEW);
            }
        });
        btnNewLog.setVisibility(allEmployees ? View.GONE : View.VISIBLE);
        lv = (PinnedSectionListView) findViewById(R.id.lvItinerary);
        TreeSet<Integer> headers = new TreeSet<>();
        ItineraryProjectDataSource ds = ItineraryProjectDataSource.getInstance(this);
        List<ItineraryProject> sectionedItems = new ArrayList<ItineraryProject>();

        try {
            ds.open();
            List<ItineraryProject> items = allEmployees ? ds.getAllItinerary() : ds.getAllItineraryByAccessCode(accessCode);

            List<String> dates = new ArrayList<String>();
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            SimpleDateFormat df2 = new SimpleDateFormat("MMM dd, yyyy");
            for(int i = 0; i < items.size(); i++) {
                ItineraryProject proj = items.get(i);
                Date date = df.parse(proj.timeIn);
                String d = df2.format(date);
                if (!dates.contains(d)) {
                    dates.add(d);
                    headers.add(i+headers.size());
                    ItineraryProject p = new ItineraryProject(0, "", d, "", "", 0, 0, "");
                    sectionedItems.add(p);
                }
                sectionedItems.add(proj);
            }
        } catch (Exception ex) {
            logError("Error loading itineraries: " + ex.getMessage());
        } finally {
            if (ds != null && ds.isOpen()) {
                ds.close();
            }
        }

        adapter = new ItineraryAdapter(this, headers, sectionedItems);
        lv.setAdapter(adapter);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                View v = view.findViewById(R.id.tvProject);
                if (v != null) {
                    ItineraryProject item = adapter.getItem(position);
                    deleteItinerary(item);
                    return true;
                }
                return false;
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                View v = view.findViewById(R.id.tvProject);
                if (v != null) {
                    Intent intent = new Intent(ItineraryProjectListActivity.this, ItineraryProjectItemActivity.class);
                    ItineraryProject item = adapter.getItem(position);
                    intent.putExtra("TIME_IN", item.timeIn);
                    intent.putExtra("TIME_OUT", item.timeOut);
                    intent.putExtra("PROJECT", item.project);
                    intent.putExtra("ACCESS_CODE", item.accessCode);
                    intent.putExtra("LOCATION", item.location);
                    intent.putExtra("LATITUDE", item.latitude);
                    intent.putExtra("LONGITUDE", item.longitude);
                    intent.putExtra("MANUAL", true);
                    intent.putExtra("ID", item.id);
                    logError("Requesting " + item.id);
                    startActivityForResult(intent, REQUEST_CODE_EDIT);
                }
            }
        });
    }

    private void deleteItinerary(final ItineraryProject project) {
        String[] logs = new String[] { "Delete", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action")
                .setItems(logs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            ItineraryProjectDataSource ds = ItineraryProjectDataSource.getInstance(ItineraryProjectListActivity.this);
                            try {
                                ds.open();
                                ds.beginTransaction();
                                ds.deleteById(project.id);
                                ds.setTransactionSuccessful();
                            } catch (Exception ex) {
                                showCrouton("Error deleting itinerary. " + ex.getMessage(), Style.ALERT);
                            } finally {
                                ds.endTransaction();
                                ds.close();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ItineraryProjectListActivity.this.updateAdapter();
                                    }
                                });
                            }
                        }
                    }
                })
                .create()
                .show();
    }

    public static int REQUEST_CODE_NEW = 0x0001;
    public static int REQUEST_CODE_EDIT = 0x0002;
    public static int REQUEST_CODE_CLOCK_OUT = 0x0003;
    public static int RESULT_CODE_NEW = 0x0013;
    public static int RESULT_CODE_EDIT = 0x0014;
    public static int RESULT_CODE_CLOCK_OUT = 0x0015;
    public static int RESULT_CODE_HOME = 0x0016;

    class ItineraryAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter {
        private List<ItineraryProject> items;
        private TreeSet<Integer> headers;

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_HEADER = 1;

        private LayoutInflater inflater;
        private Context context;

        public ItineraryAdapter(Context context, TreeSet<Integer> headers, List<ItineraryProject> items) {
            this.items = items;
            this.context = context;
            this.headers = headers;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return headers.contains(position) ? TYPE_HEADER : TYPE_ITEM;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        public void setItems(List<ItineraryProject> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        public void setHeaders(TreeSet<Integer> headers) {
            this.headers = headers;
        }

        @Override
        public ItineraryProject getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            int rowType = getItemViewType(position);

            if (convertView == null) {
                holder = new ViewHolder();
                switch (rowType) {
                    case TYPE_ITEM:
                        convertView = inflater.inflate(R.layout.layout_itinerary_project, null);
                        holder.tvProject = (TextView) convertView.findViewById(R.id.tvProject);
                        holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
                        holder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
                        holder.tvSpan = (TextView) convertView.findViewById(R.id.tvSpan);
                        holder.llEmployee = (LinearLayout) convertView.findViewById(R.id.llEmployee);
                        break;
                    case TYPE_HEADER:
                        convertView = inflater.inflate(R.layout.activity_itinerary_list_header, null);
                        holder.tvTime = (TextView) convertView.findViewById(R.id.tvDate);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ItineraryProject item = getItem(position);
            if (rowType == TYPE_HEADER) {
                holder.tvTime.setText(item.project);
            } else {
                holder.llEmployee.setVisibility(allEmployees ? View.VISIBLE : View.GONE);
                TextView tvAccessCode = (TextView) holder.llEmployee.findViewById(R.id.tvAccessCode);
                tvAccessCode.setText(item.accessCode);
                holder.tvProject.setText(item.project);
                SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                try {
                    Date dateCreated = df.parse(item.dateCreated);
                    holder.tvSpan.setText(new PrettyTime().format(dateCreated));
                } catch (Exception ex) {
                    holder.tvSpan.setText(item.dateCreated);
                    logError(ex.getMessage());
                }
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                    holder.tvTime.setText(sdf.format(df.parse(item.timeIn)) + " - " + sdf.format(df.parse(item.timeOut)));
                } catch(Exception ex) {
                    holder.tvTime.setText(item.timeIn);
                    logError(ex.getMessage());
                }
                holder.tvLocation.setText(item.location);
            }
            return convertView;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == TYPE_HEADER;
        }
    }

    public static class ViewHolder {
        public TextView tvLocation;
        public TextView tvTime;
        public TextView tvProject;
        public TextView tvSpan;
        public LinearLayout llEmployee;
    }
}