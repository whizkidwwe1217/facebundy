package com.jeonsoft.facebundypro.utils;

import android.graphics.Bitmap;

import com.neurotec.biometrics.NTemplate;
import com.neurotec.io.NBuffer;

/**
 * Created by WendellWayne on 3/15/2015.
 */
public final class BiometricUtils {
    public static Bitmap getImageFromTemplateStream(byte[] templateData) {
        NBuffer buffer = NBuffer.fromArray(templateData);
        NTemplate template = new NTemplate(buffer);
        //template.getFaces().sa
        return null;
    }
}
