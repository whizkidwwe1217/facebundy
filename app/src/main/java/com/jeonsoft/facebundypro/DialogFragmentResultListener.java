package com.jeonsoft.facebundypro;

/**
 * Created by WendellWayne on 2/17/2015.
 */
public interface DialogFragmentResultListener {
    void onResultReturned(Object value);
    void onResultWithValueReturned(boolean valid, Object value);
}
