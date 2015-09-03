package com.jeonsoft.facebundypro.views;

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
import android.widget.EditText;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.widgets.SnackBar;
import com.jeonsoft.facebundypro.DialogFragmentResultListener;
import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.R;
/**
 * Created by WendellWayne on 3/19/2015.
 */
public class ChangePinCodeDialogFragment extends DialogFragment {
    private DialogFragmentResultListener listener;
    private boolean isPINValid = false;
    private String pin = "";

    public ChangePinCodeDialogFragment() {}

    @SuppressLint("ValidFragment")
    public ChangePinCodeDialogFragment(DialogFragmentResultListener listener) {
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
        View view = inflater.inflate(R.layout.change_pin_dialog, null);
        final EditText edtPwd = (EditText) view.findViewById(R.id.edtNewPIN);
        final EditText edtConfirm = (EditText) view.findViewById(R.id.edtConfirmPIN);
        final EditText edtOldPwd = (EditText) view.findViewById(R.id.edtCurrentPIN);

        ButtonFlat btnCancel = (ButtonFlat) view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePinCodeDialogFragment.this.dismiss();
            }
        });
        ButtonFlat btnAccept = (ButtonFlat) view.findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPINValid = false;
                String p = edtPwd.getText().toString().trim();
                String c = edtConfirm.getText().toString().trim();
                String o = edtOldPwd.getText().toString().trim();

                String ref = CacheManager.getInstance(getActivity()).getStringPreference(CacheManager.ADMIN_PASSWORD);
                if (ref == null || ref.isEmpty())
                    ref = "123456";

                if (o.isEmpty()) {
                    showSnackBar("Please enter your current PIN.", "", null);
                    return;
                }

                if (p.isEmpty()) {
                    showSnackBar("Please enter your new PIN.", "", null);
                    return;
                }

                if (c.isEmpty()) {
                    showSnackBar("Please confirm your PIN.", "", null);
                    return;
                }

                if (!c.equals(p)) {
                    showSnackBar("Confirmation PIN does not match the new PIN.", "", null);
                    return;
                }

                if (c.equals(p) && o.equals(ref)) {
                    isPINValid = true;
                    pin = p;
                    ChangePinCodeDialogFragment.this.dismiss();
                } else {
                    showSnackBar("The current PIN you have entered is incorrect.", "", null);
                    return;
                }
            }
        });
        builder.setView(view);
        builder.setTitle("Enter your PIN");
        return builder.create();
    }

    public void showSnackBar(String message, String action, View.OnClickListener listener) {
        SnackBar snackbar = new SnackBar(getActivity(), message, action, listener);
        snackbar.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (listener != null)
            listener.onResultWithValueReturned(isPINValid, pin);
        super.onDismiss(dialog);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setCancelable(true);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
    }

    public static DialogFragment newInstance(Bundle bundle, DialogFragmentResultListener listener) {
        ChangePinCodeDialogFragment fragment = new ChangePinCodeDialogFragment(listener);
        fragment.setArguments(bundle);
        return fragment;
    }
}
