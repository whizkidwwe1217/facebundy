package com.jeonsoft.facebundypro.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jeonsoft.facebundypro.R;

/**
 * Created by WendellWayne on 7/12/2015.
 */
public final class DialogUtils {
    public enum DialogImageSize {
        OneByOne,
        OneByTwo,
        TwoByTwo
    }

    public static void showDialog(final Activity context, final String subtitle, final String description, final int imageResId, final DialogImageSize size) {
        DialogUtils.showDialog(context, "Can't recognize your face.", subtitle, description, imageResId, size, false, "Got it!");
    }

    public static void showDialogNoTitle(final Activity context, final String subtitle, final String description, final int imageResId, final DialogImageSize size) {
        DialogUtils.showDialog(context, "", subtitle, description, imageResId, size, true, "Got it!");
    }

    public static void showDialogNoTitle(final Activity context, final String subtitle, final String description, final int imageResId, final DialogImageSize size, final String buttonText) {
        DialogUtils.showDialog(context, "", subtitle, description, imageResId, size, true, buttonText);
    }

    public static void showDialog(final Activity context, final String title, final String subtitle, final String description, final int imageResId, final DialogImageSize size, final boolean noTitle, final String buttonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        View view = context.getLayoutInflater().inflate(R.layout.layout_dialog_image_quality, null);
        TextView tvSubtitle = (TextView) view.findViewById(R.id.tvSubtitle);
        tvSubtitle.setText(subtitle);
        TextView tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        tvDescription.setText(description);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.imageContainer);
        View imgContent = null;
        if (size == DialogImageSize.TwoByTwo)
            imgContent = context.getLayoutInflater().inflate(R.layout.dialog_image_container_2x2, null);
        else if (size == DialogImageSize.OneByTwo)
            imgContent = context.getLayoutInflater().inflate(R.layout.dialog_image_container_1x2, null);
        if (imgContent != null) {
            ImageView img = (ImageView) imgContent.findViewById(R.id.img);
            img.setImageDrawable(context.getResources().getDrawable(imageResId));
        }
        ll.addView(imgContent);

        builder.setView(view);
        builder.setCancelable(true);
        builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog d = builder.create();
        if (noTitle)
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.show();
    }

    public static void showDialog(final Activity context, final String title, final String subtitle, final String description, final Bitmap bmp, final DialogImageSize size, final boolean noTitle, final String buttonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        View view = context.getLayoutInflater().inflate(R.layout.layout_dialog_image_quality, null);
        TextView tvSubtitle = (TextView) view.findViewById(R.id.tvSubtitle);
        tvSubtitle.setText(subtitle);
        TextView tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        tvDescription.setText(description);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.imageContainer);
        View imgContent = null;
        if (size == DialogImageSize.TwoByTwo)
            imgContent = context.getLayoutInflater().inflate(R.layout.dialog_image_container_2x2, null);
        else if (size == DialogImageSize.OneByTwo)
            imgContent = context.getLayoutInflater().inflate(R.layout.dialog_image_container_1x2, null);
        if (imgContent != null && bmp != null) {
            ImageView img = (ImageView) imgContent.findViewById(R.id.img);
            img.setImageBitmap(bmp);
            ll.addView(imgContent);
        }

        builder.setView(view);
        builder.setCancelable(true);
        builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog d = builder.create();
        if (noTitle)
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.show();
    }

    public static void showQuestionDialog(final Activity context, final String subtitle, final String description, final int imageResId, final DialogImageSize size, final String positiveButtonText, final String negativeButtonText, final DialogInterface.OnClickListener actionListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = context.getLayoutInflater().inflate(R.layout.layout_dialog_image_quality, null);
        TextView tvSubtitle = (TextView) view.findViewById(R.id.tvSubtitle);
        tvSubtitle.setText(subtitle);
        TextView tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        tvDescription.setText(description);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.imageContainer);
        View imgContent = null;
        if (size == DialogImageSize.TwoByTwo)
            imgContent = context.getLayoutInflater().inflate(R.layout.dialog_image_container_2x2, null);
        else if (size == DialogImageSize.OneByTwo)
            imgContent = context.getLayoutInflater().inflate(R.layout.dialog_image_container_1x2, null);
        else if (size == DialogImageSize.OneByOne)
            imgContent = context.getLayoutInflater().inflate(R.layout.dialog_image_container_1x2, null);

        if (imgContent != null) {
            ImageView img = (ImageView) imgContent.findViewById(R.id.img);
            img.setImageDrawable(context.getResources().getDrawable(imageResId));
        }
        ll.addView(imgContent);

        builder.setView(view);
        builder.setCancelable(true);
        builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (actionListener != null) {
                    actionListener.onClick(dialog, which);
                }
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog d = builder.create();
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.show();
    }

    public enum BiometricInfoType {
        BadDynamicRange,
        BadSharpness,
        BadFacePosture,
        UnspecifiedAccessCode,
        CantIdentify,
        UnknownAccessCode,
        Confused,
        TimeOutOfSync
    }

    public static void showBiometricCaptureInfo(final Activity activity, final BiometricInfoType type) {
        showBiometricCaptureInfo(activity, type, null);
    }

    public static void showBiometricCaptureInfo(final Activity activity, final BiometricInfoType type, final DialogInterface.OnClickListener actionListener) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == BiometricInfoType.BadDynamicRange) {
                    showDialogNoTitle(activity, "Bad Dynamic Range",
                            "Make sure you have a good lighting condition and avoid glares to achieve the correct exposure.",
                            R.drawable.bad_dynamic_range, DialogImageSize.TwoByTwo);
                } else if (type == BiometricInfoType.BadSharpness) {
                    showDialogNoTitle(activity, "Bad Sharpness",
                            "Don't shake your camera too much to avoid taking a blurry photo.",
                            R.drawable.bad_sharpness, DialogImageSize.OneByTwo);
                } else if (type == BiometricInfoType.BadFacePosture) {
                    showDialogNoTitle(activity, "Bad Face Posture",
                            "Position your face at the center of the camera view finder. A neutral facial expression is recommended.",
                            R.drawable.face_position, DialogImageSize.TwoByTwo);
                } else if (type == BiometricInfoType.UnspecifiedAccessCode) {
                    showDialogNoTitle(activity, "Acess Code is Blank",
                            "Enter your access code.",
                            R.drawable.enter_access_code, DialogImageSize.TwoByTwo, "Okay");
                } else if (type == BiometricInfoType.CantIdentify) {
                    showDialogNoTitle(activity, "Can't Identify Your Face",
                            "Are you sure this is you? Is your access code correct?",
                            R.drawable.cant_identify, DialogImageSize.TwoByTwo, "Try again");
                } else if (type == BiometricInfoType.UnknownAccessCode) {
                    showDialogNoTitle(activity, "Unknown Access Code",
                            "The access code you entered is unknown. Make sure you are enrolled and you are using the correct access code.",
                            R.drawable.cant_identify, DialogImageSize.TwoByTwo);
                } else if (type == BiometricInfoType.Confused) {
                    showDialogNoTitle(activity, "Face Recognition Failure",
                            "We're having a hard time recognizing your face. Please try again.",
                            R.drawable.confused, DialogImageSize.TwoByTwo, "Okay");
                } else if (type == BiometricInfoType.TimeOutOfSync) {
                    showQuestionDialog(activity, "Date & time is out of sync!",
                            "Current date and time is not accurate. Tap the Sync button to synchronize the date and time.",
                            R.drawable.time_out_of_sync, DialogImageSize.TwoByTwo, "Close", "Sync", actionListener);
                }
            }
        });
    }
}
