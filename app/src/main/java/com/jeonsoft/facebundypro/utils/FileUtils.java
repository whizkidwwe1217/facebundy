package com.jeonsoft.facebundypro.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public final class FileUtils {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static CLibrary LIBRARY = (CLibrary) Native.loadLibrary("c", CLibrary.class);

	// ===========================================================
	// Private constructor
	// ===========================================================

	private FileUtils() {
	}

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static void copyFile(File source, File destination, boolean overwrite) throws IOException {
		InputStream fis = null;
		OutputStream fos = null;

		if (destination.exists()) {
			if (!overwrite) {
				throw new IOException(String.format("Failed to copy file. '%s' already exists.", destination.getAbsolutePath()));
			} else {
				destination.delete();
			}
		}

		try {
			fis = new FileInputStream(source);
			fos = new FileOutputStream(destination);
			IOUtils.copy(fis, fos);
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.flush();
				fos.close();
			}
		}
	}

	public static FilenameFilter getFilenameFilter(final String... extensions) {
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File file = new File(dir, name);
				for (String extension : extensions) {
					if (file.isFile() && name.endsWith(extension)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	public static FileOutputStream openOutputStream(File file) throws IOException {
		if (file == null) throw new NullPointerException("file");
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException(String.format("File %s is a directory", file));
			}
			if (file.canWrite() == false) {
				throw new IOException(String.format("File %s does not have write permissions", file));
			}
		} else {
			File parent = file.getParentFile();
			if (parent != null && parent.exists() == false) {
				if (parent.mkdirs() == false) {
					throw new IOException(String.format("File %s could not be created", file));
				}
			}
			file.createNewFile();
		}
		return new FileOutputStream(file);
	}

	public static FileInputStream openInputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException(String.format("File %s is a directory", file));
			}
			if (file.canRead() == false) {
				throw new IOException(String.format("File %s does not have read permissions", file));
			}
		} else {
			throw new FileNotFoundException(String.format("File %s does not exist", file));
		}
		return new FileInputStream(file);
	}

	public static String removeExtension(String fileName) {
		if (fileName == null) throw new NullPointerException("fileName");
		int position = fileName.lastIndexOf(".");
		if (position == -1) return fileName;
		return fileName.substring(0, position);
	}

	public static void setExecutable(File file) {
		if (file == null) throw new NullPointerException("file");
		if (!file.exists()) throw new IllegalArgumentException("file does not exist");
		LIBRARY.chmod(file.getAbsolutePath(), 0755);
	}

	public static byte[] readFileToByteArray(String path) throws IOException {
		if (path == null) throw new NullPointerException("path");
		FileInputStream fis = null;
		try {
			fis = openInputStream(new File(path));
			return IOUtils.toByteArray(fis);
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	public static String readFileToString(String path) throws IOException {
		return new String(readFileToByteArray(path));
	}

	private static boolean isPrintableChar(char c) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return (!Character.isISOControl(c)) &&
				block != null &&
				block != Character.UnicodeBlock.SPECIALS;
	}

	public static String readPrintableCharacters(String path) throws IOException {
		File file = new File(path);

		StringBuilder text = new StringBuilder();
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				for (int i = 0; i < line.length(); i++) {
					char c = line.charAt(i);
					if (!isPrintableChar(c))
						throw new RuntimeException("Invalid input file format!");
				}
				text.append(line);
				text.append("\n");
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Check sn file");
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return text.toString();
	}

//	public static String readFile(String path) {
//		String result = null;
//		try {
//			BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
//			byte[] buffer = new byte[1024];
//			int n = in.read(buffer, 0, 1024);
//			while (n >= 0) {
//				String chunk = new String(buffer, 0, n, "UTF-8");
//				result += chunk;
//				n = in.read(buffer, 0, 1024);
//			}
//			in.close();
//		} catch (Exception e) {
//			result = null;
//		}
//		return result;
//	}

	public static void write(String path, ByteBuffer buffer) throws IOException {
		if (path == null) throw new NullPointerException("path");
		if (buffer == null) throw new NullPointerException("buffer");
		FileOutputStream fos = null;
		try {
			fos = openOutputStream(new File(path));
			FileChannel channel = fos.getChannel();
			channel.write(buffer);
			channel.close();
		} finally {
			if (fos != null) {
				fos.flush();
				fos.close();
			}
		}
	}

	public static void write(String path, String value) throws IOException {
		if (path == null) throw new NullPointerException("path");
		if (value == null) throw new NullPointerException("value");
		FileOutputStream fos = null;
		try {
			fos = openOutputStream(new File(path));
			fos.write(value.getBytes());
		} finally {
			if (fos != null) {
				fos.flush();
				fos.close();
			}
		}
	}

	public static void write(String path, byte[] data) throws IOException {
		if (data == null) throw new NullPointerException("data");
		write(path, ByteBuffer.wrap(data));
	}

	interface CLibrary extends Library {
		public int chmod(String path, int mode);
	}

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

}
