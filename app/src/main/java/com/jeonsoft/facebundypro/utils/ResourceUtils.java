package com.jeonsoft.facebundypro.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.media.MediaScannerConnection;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public final class ResourceUtils {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final String TAG = ResourceUtils.class.getSimpleName();
	private static final String ASSET_COPYING_FLAG_SUFFIX = "AssetCopyingDone";

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static String getEnum(Context context, Enum<?> value) {
		if (context == null) throw new NullPointerException("context");
		if (value == null) throw new NullPointerException("value");

		int resId = context.getResources().getIdentifier(String.format("msg_%s", value.toString().toLowerCase()), "string", context.getPackageName());
		return resId == 0 ? null : context.getString(resId);
	}

	public static void copyAssets(Context context, String assetDirectoryName, String sampleDataDirName) throws IOException {
		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		String key = ResourceUtils.class.getName() + "." + assetDirectoryName + ASSET_COPYING_FLAG_SUFFIX;
		boolean done = prefs.getBoolean(key, false);
		if (!done) {
			List<String> paths = new ArrayList<String>();
			ResourceUtils.copyAssets(context.getAssets(), assetDirectoryName, sampleDataDirName, paths);
			MediaScannerConnection.scanFile(context, paths.toArray(new String[paths.size()]), null, null);
			prefs.edit().putBoolean(key, true).commit();
		}
	}

	private static void copyAssets(AssetManager manager, String assetDir, String appDataDir, List<String> paths) throws IOException {
		String[] filePaths = manager.list(assetDir);
		for (String filePath : filePaths) {
			String src = IOUtils.combinePath(assetDir, filePath).substring(1);
			File dstDir = new File(appDataDir, assetDir);
			if (!dstDir.exists()) {
				if (!dstDir.mkdirs()) {
					throw new IOException("Cannot create file: " + dstDir.getAbsolutePath());
				}
			}
			String dst = IOUtils.combinePath(dstDir.getAbsolutePath(), filePath).substring(1);
			InputStream in = null;
			OutputStream out = null;
			try {
				in = manager.open(src);
				out = new FileOutputStream(dst);
				Log.i(TAG, "Copying asset " + src + " to " + dst);
				IOUtils.copy(in, out);
				paths.add(dst);
			} catch (FileNotFoundException e) {
				// Must be a directory - ignore and continue.
			} finally {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			}
			copyAssets(manager, src, appDataDir, paths);
		}
	}

	// ===========================================================
	// Private constructor
	// ===========================================================

	private ResourceUtils() {
	}

}
