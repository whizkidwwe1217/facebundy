package com.jeonsoft.facebundypro.views;

import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by WendellWayne on 6/30/2015.
 */
public class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
    private RecyclerView recyclerView;
    private RecyclerViewOnItemClickListener clickListener;
    private ActionMode.Callback callback;
    private IRecyclerViewSelectionToggle selectionToggle;
    private BaseActionBarActivity activity;

    public RecyclerViewOnGestureListener(BaseActionBarActivity activity, IRecyclerViewSelectionToggle selectionToggle, RecyclerViewOnItemClickListener clickListener, ActionMode.Callback callback, RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.clickListener = clickListener;
        this.callback = callback;
        this.selectionToggle = selectionToggle;
        this.activity = activity;
    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        clickListener.onClick(view);
        return super.onSingleTapConfirmed(e);
    }

    public void onLongPress(MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        // Start the CAB using the ActionMode.Callback defined above
        clickListener.onLongPress(e);
        int idx = recyclerView.getChildPosition(view);
        selectionToggle.toggleSelection(idx);
        super.onLongPress(e);
    }
}