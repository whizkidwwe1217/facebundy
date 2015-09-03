package com.jeonsoft.facebundypro.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by Wayne on 4/11/2014.
 */
public class DialogAsync<P, R> extends AsyncTask<P, Void, R> {
    private ObjectCallback<P, R> callback;
    private ProgressDialog pDialog;
    private Context context;
    private String preExecuteMessage;

    public DialogAsync(Context context, ObjectCallback callback, String preExecuteMessage) {
        this.context = context;
        this.callback = callback;
        this.preExecuteMessage = preExecuteMessage;
    }

    @Override
    protected R doInBackground(P... ps) {
        R obj = null;
        try {
            obj = callback.onObjectRequestProcess(ps);
        } catch (Exception ex) {
            callback.onObjectRequestError(ex.getMessage());
        }
        return obj;
    }

    @Override
    protected void onPreExecute() {
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(preExecuteMessage);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
        super.onPreExecute();
        callback.onPreRequest();
    }

    @Override
    protected void onPostExecute(R o) {
        pDialog.dismiss();
        super.onPostExecute(o);
        callback.onObjectRequestComplete(o);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        callback.onRequestCancelled();
    }
}
