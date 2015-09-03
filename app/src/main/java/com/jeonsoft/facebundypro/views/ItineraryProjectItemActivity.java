package com.jeonsoft.facebundypro.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonFloat;
import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.data.ItineraryProject;
import com.jeonsoft.facebundypro.data.ItineraryProjectDataSource;
import com.jeonsoft.facebundypro.data.ItineraryProjectTask;
import com.jeonsoft.facebundypro.data.ItineraryProjectTaskDataSource;
import com.jeonsoft.facebundypro.location.FetchAddressIntentService;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.ConnectivityHelper;
import com.jeonsoft.facebundypro.utils.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by WendellWayne on 6/25/2015.
 */
public class ItineraryProjectItemActivity extends BaseActionBarActivity
        implements IRecyclerViewSelectionToggle,
        ActionMode.Callback, RecyclerView.OnItemTouchListener {
    private TextView tvTimeIn, tvTimeOut, tvLocation, tvDate;
    private ButtonFloat btnNewLog;
    private Calendar mCalendarIn = Calendar.getInstance();
    private Calendar mCalendarOut = Calendar.getInstance();
    private EditText edtProject;
    private boolean isEdit = false;
    private int id = -1;
    private String accessCode, project, location, timeIn, timeOut;
    private double latitude, longitude;
    private RecyclerView mListView;
    private ItineraryTaskAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_itinerary_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_fetch_from_logs) {
            showLogsDialog();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            saveProjectItinerary();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            intent.putExtra("ACCESS_CODE", accessCode);
            setResult(ItineraryProjectListActivity.RESULT_CODE_HOME, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProjectItinerary() {
        mTasks = mAdapter.getTasks();

        ItineraryProjectDataSource ids = ItineraryProjectDataSource.getInstance(this);

        project = edtProject.getText().toString();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        timeIn = df.format(mCalendarIn.getTime());
        timeOut = df.format(mCalendarOut.getTime());
        ItineraryProject proj = null;

        try {
            ids.open();
            ids.beginTransaction();
            if (isEdit)
                proj = ids.updateItinerary(accessCode, project, timeIn, timeOut, latitude, longitude, location, id);
            else
                proj = ids.createItinerary(accessCode, project, timeIn, isManual ? timeOut : "", latitude, longitude, location);
            ids.setTransactionSuccessful();
        } catch(Exception ex) {
            proj = null;
            logError("Error saving itinerary. " + ex.getMessage());
        } finally {
            if (ids != null && ids.isOpen()) {
                ids.endTransaction();
                ids.close();
            }
        }

        if (proj != null) {
            ItineraryProjectTaskDataSource tds = ItineraryProjectTaskDataSource.getInstance(this);
            try {
                tds.open();
                tds.beginTransaction();
                for(ItineraryProjectTask task : addedTasks) {
                    if (task.id == -1) {
                        tds.createItineraryTask(task.accessCode, task.timeIn, task.task, task.notes, proj.id);
                    }
                }
                tds.setTransactionSuccessful();
            } catch (Exception ex) {
                logError("Error saving itinerary task. " + ex.getMessage());
            } finally {
                if (tds != null && tds.isOpen()) {
                    tds.endTransaction();
                    tds.close();
                    addedTasks.clear();
                }
            }
        }

        // Commit deleted tasks
        ItineraryProjectTaskDataSource tds = ItineraryProjectTaskDataSource.getInstance(this);
        try {
            tds.open();
            tds.beginTransaction();
            for(int i = 0; i < deletedIds.size(); i++) {
                tds.deleteById(deletedIds.get(i));
            }
            tds.setTransactionSuccessful();
        } catch (Exception ex) {
            logError("Error deleting itinerary tasks. " + ex.getMessage());
        } finally {
            if (tds != null && tds.isOpen()) {
                tds.endTransaction();
                tds.close();
                deletedIds.clear();
            }
        }

        setResult(isEdit ? ItineraryProjectListActivity.RESULT_CODE_EDIT : ItineraryProjectListActivity.RESULT_CODE_NEW);
        finish();
    }

    /*private void deleteSelectedItems() {
        if (mAdapter != null) {
            List<Integer> items = mAdapter.getSelectedItems();
            for (int i = 0; i < items.size(); i++) {
                int key = items.get(i);
                mAdapter.getItemId()
            }
        }
    }*/

    private void showLogsDialog() {
        try {

        } catch(Exception ex) {

        } finally {

        }
        String[] logs = new String[] { "9:00 AM", "11:00 AM", "12:00 AM"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a time log")
                .setItems(logs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ACCESS_CODE", accessCode);
        logError("Saving state..." + accessCode);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        accessCode = savedInstanceState.getString("ACCESS_CODE");
        logError("Restoring state..." + accessCode);
    }

    private boolean isManual = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_itinerary_project_item);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        btnNewLog = (ButtonFloat) findViewById(R.id.btnNewLog);
        btnNewLog.setBackgroundColor(getResources().getColor(R.color.accent_color));
        tvTimeIn = (TextView) findViewById(R.id.tvTimeIn);
        tvTimeOut = (TextView) findViewById(R.id.tvTimeOut);
        edtProject = (EditText) findViewById(R.id.edtProject);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvDate = (TextView) findViewById(R.id.tvDate);

        Intent i = getIntent();
        if (savedInstanceState != null)
            accessCode = savedInstanceState.getString("ACCESS_CODE");
        else
            accessCode = getIntent().getStringExtra("ACCESS_CODE");
        logError("Access code..." + accessCode);
        latitude = getIntent().getDoubleExtra("LATITUDE", -1);
        longitude = getIntent().getDoubleExtra("LONGITUDE", -1);
        location = getIntent().getStringExtra("LOCATION");

        tvLocation.setText(location);
        isManual = i.hasExtra("MANUAL");
        if (i.hasExtra("ID")) {
            project = getIntent().getStringExtra("PROJECT");
            String timeIn = getIntent().getStringExtra("TIME_IN");
            String timeOut = getIntent().getStringExtra("TIME_OUT");
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            id = getIntent().getIntExtra("ID", -1);

            try {
                mCalendarIn = DateTimeUtils.DateToCalendar(df.parse(timeIn));
                mCalendarOut = DateTimeUtils.DateToCalendar(df.parse(timeOut));
            } catch (Exception ex) {
                logError(ex.getMessage());
            }
            edtProject.setText(project);
            isEdit = true;
        }

        SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        String strIn = getIntent().getStringExtra("TIME_IN");
        String strOut = getIntent().getStringExtra("TIME_OUT");
        if (strIn != null && !strIn.equals("null")) {
            try {
                SimpleDateFormat tdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                Date date = tdf.parse(strIn);
                mCalendarIn.setTime(date);
                tvTimeIn.setText(df.format(mCalendarIn.getTime()));
                tvDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(mCalendarIn.getTime()));
            } catch(Exception ex) {
                logError("Error parsing date." + ex.getMessage());
                tvTimeIn.setText("unspecified");
            }
        } else {
            tvTimeIn.setText("unspecified");
            tvDate.setText("unspecified");
        }
        if (strOut != null && !strOut.equals("null")) {
            try {
                SimpleDateFormat tdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                Date date = tdf.parse(strOut);
                mCalendarOut.setTime(date);
                tvTimeOut.setText(df.format(mCalendarOut.getTime()));
            } catch(Exception ex) {
                logError("Error parsing date." + ex.getMessage());
                tvTimeOut.setText("unspecified");
            }
        } else {
            tvTimeOut.setText("unspecified");
        }

        tvTimeIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = mCalendarIn;
                final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                final int minute = calendar.get(Calendar.MINUTE);
                final int year = calendar.get(Calendar.YEAR);
                final int month = calendar.get(Calendar.MONTH);
                final int dayOfMonth = calendar.get(Calendar.DATE);

                final TimePickerDialog tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i2) {
                        Calendar cal = calendar;
                        cal.set(Calendar.HOUR_OF_DAY, i);
                        cal.set(Calendar.MINUTE, i2);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                        cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                        cal.set(Calendar.DATE, calendar.get(Calendar.DATE));
                        ItineraryProjectItemActivity.this.mCalendarIn = cal;
                        ItineraryProjectItemActivity.this.updateTime(false);
                    }
                }, hour, minute, false);
                tpd.show(getFragmentManager(), "timePicker");
            }
        });

        tvTimeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = mCalendarOut;
                final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                final int minute = calendar.get(Calendar.MINUTE);
                final int year = calendar.get(Calendar.YEAR);
                final int month = calendar.get(Calendar.MONTH);
                final int dayOfMonth = calendar.get(Calendar.DATE);

                final TimePickerDialog tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i2) {
                        Calendar cal = calendar;
                        cal.set(Calendar.HOUR_OF_DAY, i);
                        cal.set(Calendar.MINUTE, i2);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                        cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                        cal.set(Calendar.DATE, calendar.get(Calendar.DATE));
                        ItineraryProjectItemActivity.this.mCalendarOut = cal;
                        ItineraryProjectItemActivity.this.updateTime(true);
                    }
                }, hour, minute, false);
                tpd.show(getFragmentManager(), "timePicker");
            }
        });

        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = mCalendarOut;
                final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                final int minute = calendar.get(Calendar.MINUTE);
                final int year = calendar.get(Calendar.YEAR);
                final int month = calendar.get(Calendar.MONTH);
                final int dayOfMonth = calendar.get(Calendar.DATE);

                final DatePickerDialog tpd = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog, int i, int i2, int i3) {
                        Calendar cal = calendar;
                        cal.set(Calendar.HOUR_OF_DAY, ItineraryProjectItemActivity.this.mCalendarIn.get(Calendar.HOUR_OF_DAY));
                        cal.set(Calendar.MINUTE, ItineraryProjectItemActivity.this.mCalendarIn.get(Calendar.MINUTE));
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        cal.set(Calendar.YEAR, i);
                        cal.set(Calendar.MONTH, i2);
                        cal.set(Calendar.DATE, i3);
                        ItineraryProjectItemActivity.this.mCalendarIn = cal;
                        cal.set(Calendar.HOUR_OF_DAY, ItineraryProjectItemActivity.this.mCalendarOut.get(Calendar.HOUR_OF_DAY));
                        cal.set(Calendar.MINUTE, ItineraryProjectItemActivity.this.mCalendarOut.get(Calendar.MINUTE));
                        ItineraryProjectItemActivity.this.mCalendarOut = cal;
                        ItineraryProjectItemActivity.this.updateTime(false);
                    }
                }, year, month, dayOfMonth);
                tpd.show(getFragmentManager(), "datePicker");
            }
        });

        mListView = (RecyclerView) findViewById(R.id.lvItinerary);
        mListView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mListView.setLayoutManager(mLayoutManager);

        ItineraryProjectTaskDataSource ds = ItineraryProjectTaskDataSource.getInstance(this);

        try {
            ds.open();
            mTasks = ds.getAllItineraryTasksByProject(id);
        } catch (Exception ex) {
            logError("Error loading itinerary tasks. " + ex.getMessage());
        } finally {
            if (ds != null && ds.isOpen())
                ds.close();
        }

        mAdapter = new ItineraryTaskAdapter(this, mTasks);
        mListView.setAdapter(mAdapter);
        mListView.addOnItemTouchListener(this);

        gestureDetector = new GestureDetectorCompat(this, new RecyclerViewOnGestureListener(this, this, new RecyclerViewOnItemClickListener() {
            @Override
            public void onClick(View view) {
                if (view != null && view.getId() == R.id.llTask) {
                    int indx = mListView.getChildPosition(view);
                    if (actionMode != null) {
                        toggleSelection(indx);
                        return;
                    }
                }
            }

            @Override
            public void onLongPress(MotionEvent event) {
                actionMode = startSupportActionMode(ItineraryProjectItemActivity.this);
            }
        }, this, mListView));

        btnNewLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTask();
            }
        });

        if (latitude != -1 && longitude != -1){
            if (ConnectivityHelper.isConnected(this))
                new GeoCodeAsync().execute();
        }
    }

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
                tvLocation.setText(location);
            }
        }
    }

    private void geoTag() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra("LATITUDE", latitude);
        intent.putExtra("LONGITUDE", longitude);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(NOTIFICATION_SERVICE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logError(intent.getStringExtra("MESSAGE"));
            tvLocation.setText(intent.getStringExtra("MESSAGE"));
        }
    };

    private List<ItineraryProjectTask> mTasks = new ArrayList<>();
    private List<ItineraryProjectTask> addedTasks = new ArrayList<>();

    private void addNewTask() {
        TaskDialogFragment d = new TaskDialogFragment();
        d.setAccessCode(accessCode);
        d.setTimeIn(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(mCalendarIn.getTime()));
        d.setListener(new TaskDialogFragment.TaskDialogFragmentListener() {
            @Override
            public void onAccept(DialogFragment dialog, ItineraryProjectTask task) {
                addedTasks.add(task);
                mAdapter.add(task);
            }
        });
        d.show(getSupportFragmentManager(), "NewItineraryTask");
    }

    private void updateTime(boolean isOut) {
        SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        if (isOut)
            tvTimeOut.setText(df.format(mCalendarOut.getTime()));
        else
            tvTimeIn.setText(df.format(mCalendarIn.getTime()));
        df = new SimpleDateFormat("MM/dd/yyyy");
        tvDate.setText(df.format(mCalendarIn.getTime()));
    }

    private List<Integer> deletedIds = new ArrayList<>();

    private void removeItemFromList(int id) {
        int key = mAdapter.getTask(id).id;
        mAdapter.remove(id);
        deletedIds.add(key);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu_itinerary_list, menu);
        btnNewLog.hide();
        btnNewLog.setVisibility(View.GONE);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_delete) {
            List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
            int currPos;
            deletedIds.clear();
            for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                currPos = selectedItemPositions.get(i);
                removeItemFromList(currPos);
            }
            actionMode.finish();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        mAdapter.clearSelections();
        btnNewLog.show();
        btnNewLog.setVisibility(View.VISIBLE);
    }

    @Override
    public void  toggleSelection(int position) {
        logError(String.valueOf(position));
        mAdapter.toggleSelection(position);
        if (actionMode != null) {
            String title = mAdapter.getSelectedItemCount() + " item(s) selected.";
            actionMode.setTitle(title);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

    }

    public static class TaskDialogFragment extends DialogFragment {
        private String accessCode, timeIn;

        public interface TaskDialogFragmentListener {
            public void onAccept(DialogFragment dialog, ItineraryProjectTask task);
        }

        public void setAccessCode(String accessCode) {
            this.accessCode = accessCode;
        }

        public void setTimeIn(String timeIn) {
            this.timeIn = timeIn;
        }

        private TaskDialogFragmentListener listener;

        public void setListener(TaskDialogFragmentListener listener) {
            this.listener = listener;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.layout_itinerary_task_editor, container, false);
            ButtonFlat btnCancel = (ButtonFlat) view.findViewById(R.id.btnCancel);
            final EditText edtTaskEdit = (EditText) view.findViewById(R.id.edtTask);
            final EditText edtNoteEdit = (EditText) view.findViewById(R.id.edtNotes);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                }
            });
            ButtonFlat btnAccept = (ButtonFlat) view.findViewById(R.id.btnAccept);
            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        String strtask = edtTaskEdit.getText().toString();
                        String strnotes = edtNoteEdit.getText().toString();
                        ItineraryProjectTask task = new ItineraryProjectTask(-1, accessCode, timeIn,  strtask, strnotes, -1);
                        listener.onAccept(TaskDialogFragment.this, task);
                    }
                    dismiss();
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
            dialog.setTitle("New Task/Activity");
            //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            return dialog;
        }
    }

    class ItineraryTaskAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<ItineraryProjectTask> items;
        private LayoutInflater inflater;
        private Context context;
        private SparseBooleanArray selectedItems;

        public ItineraryTaskAdapter(Context context, List<ItineraryProjectTask> items) {
            this.items = items;
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.selectedItems = new SparseBooleanArray();
        }

        public List<ItineraryProjectTask> getTasks() {
            return items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = inflater.inflate(R.layout.layout_itinerary_project_item_task, viewGroup, false);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        public void add(ItineraryProjectTask task) {
            items.add(task);
            notifyItemInserted(items.size()-1);
        }

        public void remove(int position) {
            items.remove(position);
            notifyItemRemoved(position);
        }

        public ItineraryProjectTask getTask(int position) {
            return items.get(position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            ItineraryProjectTask task = items.get(i);
            TextView tvTask = (TextView) viewHolder.view.findViewById(R.id.tvTask);
            TextView tvNotes = (TextView) viewHolder.view.findViewById(R.id.tvNotes);
            tvTask.setText(task.task);
            tvNotes.setText(task.notes);
            viewHolder.view.setActivated(selectedItems.get(i, false));
        }

        public void toggleSelection(int pos) {
            if (selectedItems.get(pos, false))
                selectedItems.delete(pos);
            else
                selectedItems.put(pos, true);
            notifyItemChanged(pos);
        }

        public void clearSelections() {
            selectedItems.clear();
            notifyDataSetChanged();
        }

        public int getSelectedItemCount() {
            return selectedItems.size();
        }

        public List<Integer> getSelectedItems() {
            List<Integer> items =
                    new ArrayList<Integer>(selectedItems.size());
            for (int i = 0; i < selectedItems.size(); i++) {
                items.add(selectedItems.keyAt(i));
            }
            return items;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }
    }
}