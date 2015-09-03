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

import com.neurotec.biometrics.NSubject;

import java.util.ArrayList;

/**
 * Created by WendellWayne on 2/16/2015.
 */
public class EnrolledFacesDialogFragment extends DialogFragment implements AdapterView.OnItemLongClickListener {
    private ListView lvEnrolledFaces;
    private ArrayList<String> mItems;
    private DialogFragmentResultListener listener;
    private String code;

    public EnrolledFacesDialogFragment() {}

    @SuppressLint("ValidFragment")
    public EnrolledFacesDialogFragment(DialogFragmentResultListener listener) {
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
        View view = inflater.inflate(R.layout.fragment_enrolled_faces, null);
        mItems = new ArrayList<>();
        for (NSubject subject : mSubjects) {
            mItems.add(subject.getId());
        }
        if (mItems.isEmpty())
            mItems.add("There are no records in the database");
        lvEnrolledFaces = (ListView) view.findViewById(R.id.lvEnrolledFaces);
        lvEnrolledFaces.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mItems));
        lvEnrolledFaces.setOnItemLongClickListener(this);
        builder.setView(view);
        builder.setTitle("Enrolled Face Ids");
        return builder.create();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String code = parent.getItemAtPosition(position).toString();
        this.code = code;
        Bundle bundle = new Bundle();
        DialogFragment sfd = SelectedFaceOptionsDialogFragment.newInstance(code, bundle, new DialogFragmentResultListener() {
            @Override
            public void onResultReturned(Object value) {
                EnrolledFacesDialogFragment.this.dismiss();
            }

            @Override
            public void onResultWithValueReturned(boolean valid, Object value) {

            }
        });
        sfd.show(getFragmentManager(), "selected_face_options");
        return true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (listener != null)
            listener.onResultReturned(code);
        super.onDismiss(dialog);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCancelable(true);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
    }

    private static NSubject[] mSubjects;

    public static DialogFragment newInstance(NSubject[] subjects, Bundle bundle, DialogFragmentResultListener listener) {
        mSubjects = subjects;
        EnrolledFacesDialogFragment fragment = new EnrolledFacesDialogFragment(listener);
        fragment.setArguments(bundle);
        return fragment;
    }
}
