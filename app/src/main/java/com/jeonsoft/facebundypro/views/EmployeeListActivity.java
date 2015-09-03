package com.jeonsoft.facebundypro.views;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.data.Employee;
import com.jeonsoft.facebundypro.data.EmployeeDataSource;
import com.jeonsoft.facebundypro.net.ConnectivityHelper;
import com.jeonsoft.facebundypro.net.ReachableServerHost;
import com.jeonsoft.facebundypro.net.ReachableServerHostListener;
import com.jeonsoft.facebundypro.net.ServerHostStatus;
import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.uploadservice.EmployeeListDownloadService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WendellWayne on 5/12/2015.
 */
public class EmployeeListActivity extends BaseActionBarActivity {
    private ListView lv;
    private EmployeeListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employees_list);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_employee_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_synch) {
            syncData();
            return true;
        } else if (item.getItemId() == R.id.action_clear) {
            this.adapter.clear();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int current = intent.getIntExtra(EmployeeListDownloadService.CURRENT, 0);
            int total = intent.getIntExtra(EmployeeListDownloadService.TOTAL, 0);
            logDebug("Receiving..." + String.valueOf(current) + " of " + String.valueOf(total));
            if (current == total) {
                logError("Completed.");
                EmployeeListActivity.this.updateAdapter();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(EmployeeListDownloadService.DOWNLOAD_SERVICE_NOTIFICATION));
    }

    private void updateAdapter() {
        EmployeeListAdapter adapter = null;
        if (lv != null)
            adapter = (EmployeeListAdapter) lv.getAdapter();
        if (adapter != null) {
            EmployeeDataSource ds = EmployeeDataSource.getInstance(this);
            try {
                if (!ds.isOpen())
                    ds.open();
                List<Employee> employees = ds.getAllEmployees();
                ArrayList<EmployeeListItem> items = new ArrayList<>();
                for (int i = 0; i < employees.size(); i++) {
                    Employee employee = employees.get(i);
                    EmployeeListItem item = new EmployeeListItem(employee.getAccessCode(), employee.getName());
                    items.add(item);
                }
                adapter.setData(items);
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

    private void syncData() {
        if(ConnectivityHelper.isConnected(this)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] hosts = CacheManager.getInstance(EmployeeListActivity.this).getStringPreference(CacheManager.SERVER_HOSTS).split("\n"); //new String[] {"http://10.0.0.82:3003", "http://activation.facebundy.com"};
                    new ReachableServerHost(EmployeeListActivity.this, hosts, new ReachableServerHostListener() {
                        @Override
                        public void onStatusChanged(ServerHostStatus status, String host) {
                            logDebug(status.toString() + ": " + host);
                        }

                        @Override
                        public void onReachableHostAcquired(String reachableHost) {
                            Intent intent = new Intent(EmployeeListActivity.this, EmployeeListDownloadService.class);
                            intent.putExtra("URL", reachableHost);
                            startService(intent);
                        }

                        @Override
                        public void onFailedHostAcquisition(String message) {
                            showSnackBar(message, "Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    syncData();
                                }
                            });
                        }
                    }).execute();
                }
            });
        } else {
            showSnackBar("Please connect to the internet to download employee list.", "Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EmployeeListActivity.this.syncData();
                }
            });
        }
    }

    private void init() {
        lv = (ListView) findViewById(R.id.lvEmployees);
        EmployeeDataSource ds = EmployeeDataSource.getInstance(this);
        try {
            ds.open();
            List<Employee> employees = ds.getAllEmployees();
            ArrayList<EmployeeListItem> items = getEmployeeListItems(employees);
            adapter = new EmployeeListAdapter(this, R.layout.employees_list_item, items);
            lv.setAdapter(adapter);
            View view = getLayoutInflater().inflate(R.layout.empty_view, null);
            lv.setEmptyView(view);
        } catch (Exception ex) {
            logError(ex.getMessage());
        } finally {
             if (ds!=null)
                 ds.close();
        }
    }

    private ArrayList<EmployeeListItem> getEmployeeListItems(List<Employee> employees) {
        ArrayList<EmployeeListItem> items = new ArrayList<>();
        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            items.add(new EmployeeListItem(emp.getAccessCode(), emp.getName()));
        }
        return items;
    }

    class EmployeeListAdapter extends ArrayAdapter<EmployeeListItem> {
        private Context context;
        private int layoutResourceId;
        private ArrayList<EmployeeListItem> data;

        public EmployeeListAdapter(Context context, int layoutResourceId, ArrayList<EmployeeListItem> data) {
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
                EmployeeListItem emp = data.get(position);
                TextView accessCode = (TextView) convertView.findViewById(R.id.tvAccessCode);
                accessCode.setTag(emp.AccessCode);
                accessCode.setText(emp.AccessCode);

                TextView name = (TextView) convertView.findViewById(R.id.tvName);
                name.setTag(emp.AccessCode);
                name.setText(emp.Name);
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
            EmployeeDataSource ds = EmployeeDataSource.getInstance(EmployeeListActivity.this);
            try {
                ds.open();
                ds.clear();
            } catch (Exception ex) {
                logError(ex.getMessage());
            } finally {
                if (ds != null)
                    ds.close();
            }
            notifyDataSetChanged();
        }

        public void setData(ArrayList<EmployeeListItem> data) {
            this.data = data;
            this.notifyDataSetChanged();
        }
    }

    class EmployeeListItem {
        public EmployeeListItem(String accessCode, String name) {
            this.AccessCode = accessCode;
            this.Name = name;
        }
        public String AccessCode;
        public String Name;
    }
}
