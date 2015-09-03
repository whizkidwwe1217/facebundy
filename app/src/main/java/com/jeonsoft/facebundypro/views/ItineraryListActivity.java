package com.jeonsoft.facebundypro.views;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jeonsoft.facebundypro.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by WendellWayne on 6/25/2015.
 */
public class ItineraryListActivity extends BaseActionBarActivity {
    private ListView lv;
    private ItineraryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary_list);

        init();
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

        lv = (ListView) findViewById(R.id.lvItinerary);
        List<ItineraryItem> items = new ArrayList<>();
        TreeSet<Integer> headers = new TreeSet<>();
        for (int i = 0; i < 20; i++) {
            items.add(new ItineraryItem(i, 0, 0, "Quezon City", "Hello World",
                    Calendar.getInstance().getTime(),
                    Calendar.getInstance().getTime(),
                    Calendar.getInstance().getTime(),
                    "1060", "Wendell Wayne Estrada"));
            if (i % 4 == 0) {
                headers.add(i);
            }
        }
        adapter = new ItineraryAdapter(this, headers, items);
    }

    class ItineraryItem {
        public int id;
        public double latitude;
        public double longitude;
        public String location;
        public String activity;
        public Date date;
        public Date creationDate;
        public Date time;
        public String accessCode;
        public String employeeName;

        public ItineraryItem(int id, double latitude, double longitude, String location, String activity, Date date, Date creationDate, Date time, String accessCode, String employeeName) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
            this.location = location;
            this.activity = activity;
            this.date = date;
            this.creationDate = creationDate;
            this.time = time;
            this.accessCode = accessCode;
            this.employeeName = employeeName;
        }
    }

    class ItineraryAdapter extends BaseAdapter {
        private List<ItineraryItem> items;
        private TreeSet<Integer> headers;

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_HEADER = 1;

        private LayoutInflater inflater;
        private Context context;

        public ItineraryAdapter(Context context, TreeSet<Integer> headers, List<ItineraryItem> items) {
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

        @Override
        public ItineraryItem getItem(int position) {
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
                        convertView = inflater.inflate(R.layout.activity_itinerary_list_item, null);
                        holder.imageView = (CircleImageView) convertView.findViewById(R.id.imgAvatar);
                        holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
                        holder.tvActivity = (TextView) convertView.findViewById(R.id.tvActivity);
                        holder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
                        holder.tvCreation = (TextView) convertView.findViewById(R.id.tvCreationTime);
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
            return convertView;
        }
    }

    public static class ViewHolder {
        public CircleImageView imageView;
        public TextView tvLocation;
        public TextView tvTime;
        public TextView tvCreation;
        public TextView tvActivity;
    }
}
