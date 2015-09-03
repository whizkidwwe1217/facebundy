package com.jeonsoft.facebundypro.biometrics.licensing;

import android.content.Context;
import android.util.Log;

import com.jeonsoft.facebundypro.utils.ResourceUtils;
import com.neurotec.licensing.NLicensingService;

import java.util.EnumSet;

public enum LicensingMode {

	DIRECTLY(true, true, false),
	FROM_PC(false, true, true),
	FROM_FILE(true, true, false);

	private static final String TAG = LicensingMode.class.getSimpleName();

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static LicensingMode get(int ordinal) {
		return LicensingMode.values()[ordinal];
	}

	public static LicensingMode get(String name) {
		return Enum.valueOf(LicensingMode.class, name);
	}

	public static EnumSet<LicensingMode> getAvailable() {
		try {
			return NLicensingService.isTrial() ? EnumSet.of(DIRECTLY, FROM_PC) : EnumSet.of(FROM_PC, FROM_FILE);
		} catch (Throwable e) {
			Log.e(TAG, "Exception", e);
			return EnumSet.allOf(LicensingMode.class);
		}
	}

	public static LicensingMode getDefault() {
		try {
			return NLicensingService.isTrial() ? DIRECTLY : FROM_FILE;
		} catch (Throwable e) {
			Log.e(TAG, "Exception", e);
			return FROM_FILE;
		}
	}

	// ===========================================================
	// Private fields
	// ===========================================================

	private boolean mPGRequired;
	private boolean mInternetBased;
	private boolean mServerConfigurable;

	// ===========================================================
	// Private constructor
	// ===========================================================

	private LicensingMode(boolean pgRequired, boolean internetBased, boolean serverConfigurable) {
		this.mPGRequired = pgRequired;
		this.mInternetBased = internetBased;
		this.mServerConfigurable = serverConfigurable;
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public boolean isPGRequired() {
		return mPGRequired;
	}

	public boolean isInternetBased() {
		return mInternetBased;
	}

	public boolean isServerConfigurable() {
		return mServerConfigurable;
	}

	public String getName(Context context) {
		return ResourceUtils.getEnum(context, this);
	}
}
