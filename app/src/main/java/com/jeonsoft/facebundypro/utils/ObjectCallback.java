package com.jeonsoft.facebundypro.utils;

public interface ObjectCallback<P, R> {
    R onObjectRequestProcess(P... params);

    void onObjectRequestComplete(R result);

    void onObjectRequestError(String message);

    void onRequestCancelled();

    void onPreRequest();
}
