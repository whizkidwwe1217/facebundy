package com.jeonsoft.facebundypro.authentication;

/**
 * Created by WendellWayne on 3/5/2015.
 */
public interface IKeyGenerator {
    String encode();
    String encode(IKeyFormatter formatter);
    ILicenseInfo decode(String inputKey) throws Exception;
}
