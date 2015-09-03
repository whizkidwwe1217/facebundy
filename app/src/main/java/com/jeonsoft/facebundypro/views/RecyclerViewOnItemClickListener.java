package com.jeonsoft.facebundypro.views;

import android.support.v7.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by WendellWayne on 7/27/2015.
 */
public interface RecyclerViewOnItemClickListener {
    void onClick(View view);
    void onLongPress(MotionEvent event);
}
