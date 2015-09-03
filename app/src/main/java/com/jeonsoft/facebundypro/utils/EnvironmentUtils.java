package com.jeonsoft.facebundypro.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public final class EnvironmentUtils {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final String TAG = EnvironmentUtils.class.getSimpleName();
	private static final int MIN_PORT_NUMBER = 1;
	private static final int MAX_PORT_NUMBER = 49151;

	// ===========================================================
	// Public static fields
	// ===========================================================

	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String PATH_SEPARATOR = System.getProperty("path.separator");
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String DATE_FORMAT = "yyyy-MM-dd-kk-mm-ss";
	public static final String NEUROTECHNOLOGY_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + FILE_SEPARATOR + "Neurotechnology";
	public static final String REPORTS_DIRECTORY_PATH = getDataDirectoryPath("Reports");
	public static final String SAMPLE_DATA_DIR_NAME = "Data";

	// ===========================================================
	// Private constructor
	// ===========================================================

	private EnvironmentUtils() {
	};

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static File getDataDirectory() {
		if (!isSdPresent()) return null;
		File directory = null;
		try {
			directory = new File(NEUROTECHNOLOGY_DIRECTORY);
			if (!directory.exists()) {
				directory.mkdirs();
			}
		} catch (SecurityException e) {
			Log.e(TAG, "Exception", e);
		}
		return directory;

	}

	public static String getDataDirectoryPath(String... pathComponents) {
		String path = NEUROTECHNOLOGY_DIRECTORY;
		for (String dir : pathComponents) {
			path = IOUtils.combinePath(path, dir);
		}
		return path;
	}

	public static File getDataDirectory(String... pathComponents) {
		File directory = new File(getDataDirectoryPath(pathComponents));
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}

	public static boolean isSdPresent() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public static boolean hasInternetConnection() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public static Integer getProcessPid(String filename) {
		File procDirectory = new File("/proc");
		if (procDirectory.exists() && procDirectory.isDirectory()) {
			File[] processes = procDirectory.listFiles();
			for (File processDirectory : processes) {
				if (processDirectory.isDirectory()) {
					File cmdLine = new File(processDirectory, "cmdline");
					if (cmdLine.exists() && cmdLine.isFile()) {
						BufferedReader br;
						try {
							br = new BufferedReader(new FileReader(cmdLine), 1024);
							String line = br.readLine();
							if (line != null) {
								if (line.contains(filename)) {
									return Integer.decode(processDirectory.getName());
								}
							}
						} catch (FileNotFoundException e) {
							Log.e(TAG, "FileNotFoundException", e);
						} catch (IOException e) {
							Log.e(TAG, "IOException", e);
						} catch (NumberFormatException e) {
							Log.e(TAG, "NumberFormatException", e);
						}
					}
				}
			}
		}
		return null;
	}

	//probably causes License.obtain to hang if checking wether port 5000 is opened
	public static boolean isPortAvailable(int port) {
		if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					Log.e(TAG, "IOException", e);
				}
			}
		}

		return false;
	}

	public static boolean isUSBHostAPISupported() {
		return android.os.Build.VERSION.SDK_INT >= 12;
	}
}
