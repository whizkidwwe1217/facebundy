package com.jeonsoft.facebundypro.biometrics.face;

import android.util.Log;

import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.plugins.NPlugin;

public final class Model {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static Model sInstance;

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static Model getInstance() {
		synchronized (Model.class) {
			if (sInstance == null) {
				sInstance = new Model();
			}
			return sInstance;
		}
	}

	// ===========================================================
	// Private fields
	// ===========================================================

	private NBiometricClient mClient;
	private NSubject mSubject;

	private NSubject[] mSubjects;

	// ===========================================================
	// Private constructor
	// ===========================================================

	private Model() {
		mClient = new NBiometricClient();
		mClient.setDatabaseConnectionToSQLite(GlobalConstants.getInstance().getContext().getDatabasePath(GlobalConstants.DATABASE_NAME).getAbsolutePath());
        mClient.setUseDeviceManager(true);
        //mClient.setFacesCreateThumbnailImage(true);
        //mClient.setFacesThumbnailImageWidth(90);
        mSubjects = new NSubject[]{};
		mSubject = new NSubject();
		for (NPlugin plugin : NDeviceManager.getPluginManager().getPlugins()) {
			Log.i("Model", String.format("Plugin name => %s, Error => %s", plugin.getModule().getName(), plugin.getError()));
		}
		for (NDevice device : mClient.getDeviceManager().getDevices()) {
			Log.i("Device", String.format("Device name => %s", device.getDisplayName()));
		}
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public NBiometricClient getClient() {
		return mClient;
	}

	public NSubject getSubject() {
		return mSubject;
	}

	/**
	 * Subjects contain copy of subject list from biometric client
	 * so that list could be accessible while continuous tasks are being
	 * performed on biometric client like capturing from camera
	 */
	public NSubject[] getSubjects() {
		return mSubjects;
	}

	/**
	 * Subjects contain copy of subject list from biometric client
	 * so that list could be accessible while continuous tasks are being
	 * performed on biometric client like capturing from camera
	 */
	public void setSubjects(NSubject[] subjects) {
		this.mSubjects = subjects;
	}
}
