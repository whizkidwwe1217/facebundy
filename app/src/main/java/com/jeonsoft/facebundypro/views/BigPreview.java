package com.jeonsoft.facebundypro.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.jeonsoft.facebundypro.R;

import java.io.File;

/**
 * Created by Wendell Wayne on 12/23/2014.
 */
public class BigPreview extends Activity {
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.big_preview);
        img = (ImageView) findViewById(R.id.imgBigImagePreview);

        Intent intent = getIntent();
        String filename = intent.getStringExtra("filename");
        if (filename == null) filename = "";
        Log.e("Jeonsoft", filename);
        File file = new File(filename);
        if (file.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(intent.getStringExtra("filename"), options);
            img.setImageBitmap(bitmap);
        }
    }
}
