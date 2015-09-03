package com.jeonsoft.facebundypro.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.neurotec.images.NImage;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by WendellWayne on 5/25/2015.
 */
public final class NImageUtils {
    public static Bitmap bitmapFromNImage(NImage nImage, Context context) {
        String path = GlobalConstants.getCacheDir(context) + "\\temp_prev_photo.jpg";
        Bitmap bitmap = null;
        try {
            nImage.save(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeFile(path, options);
        } catch(Exception ex) {
            Logger.logE(ex.getMessage());
        } finally {
            File file = new File(path);
            file.delete();
        }
        return bitmap;
    }

    public static Bitmap fromData(byte[] data, int width, int height) {
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, baos);
        byte[] bitmapData = baos.toByteArray();
        return BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
    }
}
