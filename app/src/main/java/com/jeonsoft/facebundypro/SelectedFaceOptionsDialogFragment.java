package com.jeonsoft.facebundypro;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by WendellWayne on 2/17/2015.
 */
public class SelectedFaceOptionsDialogFragment extends DialogFragment {
    private ListView lvSelectedFaceOptions;
    private ArrayList<String> mItems;
    private DialogFragmentResultListener listener;
    private int position = -1;

    public SelectedFaceOptionsDialogFragment() {}

    @SuppressLint("ValidFragment")
    public SelectedFaceOptionsDialogFragment(DialogFragmentResultListener listener) {
        this.listener = listener;
    }
    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_selected_face_options, null);
        mItems = new ArrayList<>();
        mItems.add("Delete");
        mItems.add("Nothing");
        lvSelectedFaceOptions = (ListView) view.findViewById(R.id.lvSelectedFaceOptions);
        lvSelectedFaceOptions.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mItems));
        lvSelectedFaceOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // delete
                    SelectedFaceOptionsDialogFragment.this.position = position;
                } else {
                    SelectedFaceOptionsDialogFragment.this.position = -1;
                }
                SelectedFaceOptionsDialogFragment.this.dismiss();
            }
        });
        builder.setView(view);
        builder.setTitle("What do you want to do with this?");
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (listener != null)
            listener.onResultReturned(position);
        super.onDismiss(dialog);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCancelable(true);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
    }

    public static DialogFragment newInstance(String id, Bundle bundle, DialogFragmentResultListener listener) {
        SelectedFaceOptionsDialogFragment fragment = new SelectedFaceOptionsDialogFragment(listener);
        fragment.setArguments(bundle);
        return fragment;
    }
}
