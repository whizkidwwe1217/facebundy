package com.jeonsoft.facebundypro.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public final class IOUtils {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final String TAG = IOUtils.class.getSimpleName();

	// ===========================================================
	// Private constructor
	// ===========================================================

	private IOUtils() {
	};

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[inputStream.available()];
		inputStream.read(buffer);
		inputStream.close();
		return buffer;
	}

	public static byte[] toByteArray(ByteBuffer buffer) {
		if (buffer == null) throw new NullPointerException("buffer");
		byte[] data = new byte[buffer.remaining()];
		buffer.get(data, 0, data.length);
		return data;
	}

	public static ByteBuffer toByteBuffer(InputStream inputStream) throws IOException {
		return ByteBuffer.wrap(toByteArray(inputStream));
	}

	public static ByteBuffer toByteBuffer(Context context, Uri uri) throws IOException {
		if (context == null) throw new NullPointerException("context");
		if (uri == null) throw new NullPointerException("uri");

		InputStream inputStream = null;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
			return IOUtils.toByteBuffer(inputStream);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(TAG, "Error closing InputStream", e);
				}
			}
		}
	}

	public static String combinePath(String... folders) {
		String path = "";
		for (String folder : folders) {
			path = path.concat(EnvironmentUtils.FILE_SEPARATOR).concat(folder);
		}
		return path;
	}

	public static void copy(InputStream source, OutputStream destination) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = source.read(buffer)) != -1) {
			destination.write(buffer, 0, read);
		}
	}

	public static boolean isIPAddressValid(String value) {
		String[] parts = value.split("\\.");
		if (parts.length != 4) {
			return false;
		}
		try {
			for (String s : parts) {
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255)) {
					return false;
				}
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

}
