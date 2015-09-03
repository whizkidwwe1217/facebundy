package com.jeonsoft.facebundypro.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.hardware.Camera;

import com.jeonsoft.facebundypro.utils.ImageUtils;

import java.io.File;

public class CameraFaceLayer {
    private Paint paintLog;
    private Paint paintImagePreview;
    private RectF rectImagePreview;
    private int width, height;
    private Paint paintLine;
    private Paint paintCircleLine;
    private Paint paintCircleFill;
    private int max = 20;
    private int x, y, w, h;
    private Path path;
    private DashPathEffect dpe;
    private PathDashPathEffect pdpe;
    private PathMeasure measure;
    private float pathLength;
    private final float tsv = 1.618f;
    private float pathPart;
    private RectF oval;

    public CameraFaceLayer(int widthScr, int heightScr) {
        w = (int) (2 * widthScr / 4);
        h = (int) (w * tsv);
        x = (int) ((widthScr - w) / 2);
        y = (int) ((heightScr - h) / 2);

        oval = new RectF(x, y, x + w, y + h);
        path = new Path();
        path.addOval(oval, Path.Direction.CW);

        measure = new PathMeasure(path, true);
        pathLength = measure.getLength();

        pathPart = pathLength / max;

        dpe = new DashPathEffect(new float[] { 5, 10 }, 0);
        pdpe = new PathDashPathEffect(makeCirclePathDash(10), pathPart, 0, PathDashPathEffect.Style.ROTATE);

        initPaint();
        this.width = widthScr;
        this.height = heightScr;
    }

    private void initPaint() {
        rectImagePreview = new RectF(10, 10, 100, 100);
        paintImagePreview = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintImagePreview.setStrokeWidth(1);
        paintImagePreview.setColor(0xFF00FF00);
        paintImagePreview.setAntiAlias(true);
        paintImagePreview.setStyle(Paint.Style.STROKE);

        paintLog = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLog.setColor(0xFF00FF00);
        paintLog.setAntiAlias(true);

        this.paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paintLine.setDither(true);
        this.paintLine.setColor(0xff23bd1d);
        this.paintLine.setStyle(Paint.Style.STROKE);
        this.paintLine.setStrokeJoin(Paint.Join.ROUND);
        this.paintLine.setStrokeCap(Paint.Cap.ROUND);
        this.paintLine.setStrokeWidth(2);

        this.paintCircleLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paintCircleLine.setColor(0xff23bd1d);
        this.paintCircleLine.setARGB(255, 255, 255, 255);
        this.paintCircleLine.setStyle(Paint.Style.STROKE);
        this.paintCircleLine.setStrokeWidth(2);

        this.paintCircleFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paintCircleFill.setColor(0xFF00FF00);
        this.paintCircleFill.setStyle(Paint.Style.FILL);
    }

    private Path makeCirclePathDash(float r) {
        Path path = new Path();
        path.addCircle(r, r, r, Path.Direction.CW);
        return path;
    }

    public void drawBorderPath(Canvas canvas) {
        paintLine.setPathEffect(dpe);
        canvas.drawPath(path, paintLine);
    }

    public void drawCirclePath(Canvas canvas) {
        paintLine.setPathEffect(pdpe);
        canvas.drawPath(path, paintLine);
    }

    private float[] getPoint(int index) {
        float pos[] = { 0f, 0f };
        measure.getPosTan(pathLength * index / max, pos, null);
        return pos;
    }

    public void drawCircleArc(Canvas canvas, int number) {
        for (int i = 0; i < max; i++) {
            float[] p = getPoint(i);
            canvas.drawCircle(p[0], p[1], 7, paintCircleLine);
        }

        for (int i = 0; i < number && i < max; i++) {
            int tmp = i + 15;
            int pos = (tmp < max ? tmp : tmp - max);
            float[] p = getPoint(pos);
            canvas.drawCircle(p[0], p[1], 6, paintCircleFill);
        }
    }

    public void drawFocusArea(Canvas canvas) {
        canvas.drawRect(rectImagePreview, paintImagePreview);
    }

    private Camera.Face[] faces;

    public void setFaces(Camera.Face[] faces) {
        this.faces = faces;
    }

    public void drawFaceDetection(Canvas canvas, int width, int height) {
        this.width = width;
        this.height = height;
        if (faces != null) {
            for (int i = 0; i < faces.length; i++) {
                Camera.Face face = faces[i];
                RectF rectF = new RectF(face.rect);
                Matrix matrix = new Matrix();
                matrix.setScale(1, 1);
                matrix.postScale(width / 2000f, height / 2000f);
                matrix.postTranslate(width / 2f, height / 2f);
                matrix.mapRect(rectF);
                canvas.drawRect(rectF, paintImagePreview);
                canvas.drawText("Left: " + String.valueOf(face.rect.left) + ", Top: "
                    + String.valueOf(face.rect.top) + ", Right: " + String.valueOf(face.rect.right),
                    150, 150, paintImagePreview);
            }
        } else {
            canvas.drawText("No face detected", 100, 100, paintImagePreview);
        }
    }

    public void drawTakenPhoto(Context context, Canvas canvas, String imagePath) {
        canvas.drawRect(rectImagePreview, paintImagePreview);

        if (imagePath != null) {
            File file = new File(imagePath);
            if (file.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                /* Flip and rotate image */
                Matrix mtx = new Matrix();
                mtx.setScale(-1, 1);
                mtx.postTranslate(bitmap.getWidth(), 0);
                mtx.postRotate(450);

                //Bitmap bmp = Bitmap.createScaledBitmap(bitmap, (int)rectImagePreview.width(), (int)rectImagePreview.height(), true);
                //Bitmap scaled = Bitmap.createBitmap(bmp, 0, 0, (int)rectImagePreview.width(), (int)rectImagePreview.height(), mtx, true);


                Bitmap scaledBitmap = ImageUtils.createScaledBitmap(bitmap, (int) rectImagePreview.width(), (int) rectImagePreview.height(), ImageUtils.ScalingLogic.CROP);

                canvas.drawBitmap(scaledBitmap, null, rectImagePreview, null);
            }
        }
    }
}