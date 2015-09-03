package com.jeonsoft.facebundypro.biometrics.licensing;

import com.jeonsoft.facebundypro.utils.FileUtils;
import com.jeonsoft.facebundypro.utils.IOUtils;
import com.neurotec.licensing.NLicense;

import java.io.File;
import java.io.IOException;

public final class License {

	// ===========================================================
	// Private fields
	// ===========================================================

	private String mFolderPath;
	private String mName;
	private String mLicenseFilePath;
	private String mSerialNumberPath;
	private String mDeviceIDPath;
	private String mDeactivationIDPath;

	// ===========================================================
	// Package private constructor
	// ===========================================================

	License(String folderPath, String name) {
		if (folderPath == null) throw new NullPointerException("folderPath");
		if (name == null) throw new NullPointerException("name");
		this.mFolderPath = folderPath;
		this.mName = name;
		this.mLicenseFilePath = IOUtils.combinePath(mFolderPath, mName + LicensingServiceManager.EXTENSION_LICENSE_FILE);
		this.mSerialNumberPath = IOUtils.combinePath(mFolderPath, mName + LicensingServiceManager.EXTENSION_SERIAL_NUMBER_FILE);
		this.mDeviceIDPath = IOUtils.combinePath(mFolderPath, mName + LicensingServiceManager.EXTENSION_DEVICE_ID_FILE);
		this.mDeactivationIDPath = IOUtils.combinePath(mFolderPath, mName + "_deactivation" + LicensingServiceManager.EXTENSION_DEVICE_ID_FILE);
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private void delete() {
		new File(mLicenseFilePath).delete();
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public boolean isActivated() {
		return new File(mLicenseFilePath).exists();
	}

	public boolean hasSerialNumber() {
		return new File(mSerialNumberPath).exists();
	}

	public void activate(boolean online) throws IOException {
		if (hasSerialNumber()) {
			String serialNumber = FileUtils.readPrintableCharacters(mSerialNumberPath);
			String deviceID = NLicense.generateID(serialNumber);
			if (deviceID != null) {
				FileUtils.write(mDeviceIDPath, deviceID);
				if (online) {
					String license = NLicense.activateOnline(deviceID);
					if (license != null) {
						FileUtils.write(mLicenseFilePath, license);
					}
				}
			}
		}
	}

	public void deactivate(boolean online) throws IOException {
		if (isActivated()) {
			String license = FileUtils.readPrintableCharacters(mLicenseFilePath);
			if (online) {
				NLicense.deactivateOnline(license);
				if (hasSerialNumber()) {
					delete();
				}
			} else {
				String deactivationID = NLicense.generateDeactivationIDForLicense(license);
				if (deactivationID != null) {
					FileUtils.write(mDeactivationIDPath, deactivationID);
				}
			}
		}
	}

	public String getName() {
		return mName;
	}

	public String getLicensePath() {
		return mLicenseFilePath;
	}

	@Override
	public String toString() {
		return mName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		result = prime * result + ((mFolderPath == null) ? 0 : mFolderPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		License other = (License) obj;
		if (mName == null) {
			if (other.mName != null) return false;
		} else if (!mName.equals(other.mName)) return false;
		if (mFolderPath == null) {
			if (other.mFolderPath != null) return false;
		} else if (!mFolderPath.equals(other.mFolderPath)) return false;
		return true;
	}

}
