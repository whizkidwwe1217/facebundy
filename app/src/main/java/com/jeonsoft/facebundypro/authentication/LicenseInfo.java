package com.jeonsoft.facebundypro.authentication;

/**
 * Created by WendellWayne on 3/5/2015.
 */
public class LicenseInfo implements ILicenseInfo {
    private int companyId;
    private int licenseNo;
    private int edition;

    public LicenseInfo(int companyId, int licenseNo, int edition) {
        this.companyId = companyId;
        this.licenseNo = licenseNo;
        this.edition = edition;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(int licenseNo) {
        this.licenseNo = licenseNo;
    }

    public int getEdition() {
        return edition;
    }

    public void setEdition(int edition) {
        this.edition = edition;
    }
}