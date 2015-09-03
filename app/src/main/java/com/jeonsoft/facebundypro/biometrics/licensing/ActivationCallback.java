package com.jeonsoft.facebundypro.biometrics.licensing;

/**
 * Created by WendellWayne on 3/6/2015.
 */
public interface ActivationCallback {
    void onActivate(boolean activated);
    void onDeactivate(boolean deactivated);
}
