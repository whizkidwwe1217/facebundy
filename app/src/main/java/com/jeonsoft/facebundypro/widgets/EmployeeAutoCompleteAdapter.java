package com.jeonsoft.facebundypro.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.data.Employee;
import com.jeonsoft.facebundypro.data.EmployeeDataSource;
import com.jeonsoft.facebundypro.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WendellWayne on 5/13/2015.
 */
public class EmployeeAutoCompleteAdapter extends BaseAdapter implements Filterable {
    private static final int MAX_RESULTS = 10;
    private Context context;
    private List<EmployeeAdapterItem> data = new ArrayList<>();
    private int layoutResourceId;

    public EmployeeAutoCompleteAdapter(Context context, int layoutResourceId) {
        this.context = context;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }
        if(data.size() > 0) {
            EmployeeAdapterItem emp = data.get(position);
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
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    ArrayList<EmployeeAdapterItem> items = findEmployee(context, constraint.toString());
                    filterResults.values = items;
                    filterResults.count = items.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    data = (ArrayList<EmployeeAdapterItem>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    private ArrayList<EmployeeAdapterItem> findEmployee(Context context, String accessCode) {
        ArrayList<EmployeeAdapterItem> employeeList = new ArrayList<>();
        EmployeeDataSource ds = EmployeeDataSource.getInstance(context);
        try {
            ds.open();
            /*List<Employee> emps = ds.getEmployee(accessCode);
            for (int i = 0; i < emps.size(); i++) {
                Employee e = emps.get(i);
                employeeList.add(new EmployeeAdapterItem(e.getAccessCode(), e.getName()));
            }*/
            Employee e = ds.getEmployee(accessCode);
            employeeList.add(new EmployeeAdapterItem(e.getAccessCode(), e.getName()));
        } catch (Exception ex) {
            Logger.logE(ex.getMessage());
        } finally {
            if (ds != null)
                ds.close();
        }
        return employeeList;
    }
}
