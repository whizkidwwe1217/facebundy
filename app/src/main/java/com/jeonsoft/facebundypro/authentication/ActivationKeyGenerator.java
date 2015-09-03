package com.jeonsoft.facebundypro.authentication;

import com.jeonsoft.facebundypro.utils.StringUtils;

import java.util.Random;

/**
 * Created by WendellWayne on 3/4/2015.
 */
public class ActivationKeyGenerator implements IKeyGenerator {
    public static final String PRIVATE_KEY = "ES1UQP35";
    public static final int BASE = 32;
    public static final int MIN = 0;
    public static final int MAX = BASE - 1;
    public static final int MAX_PAD = 3;
    public static final char PAD_CHAR = '0';

    private LicenseInfo licenseInfo;

    public ActivationKeyGenerator(LicenseInfo licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    public ActivationKeyGenerator() {

    }

    @Override
    public String encode(IKeyFormatter formatter) {
        int checksum = calculateChecksum(licenseInfo.getCompanyId(),
                licenseInfo.getLicenseNo(), licenseInfo.getEdition());
        int ran = generateRandomInRange(MIN, MAX);
        String salt = String.valueOf(decimalToBase(ran));
        String privateKey = addSalt(PRIVATE_KEY, salt);

        String activationKey =
                StringUtils.leftPad(decimalToBase(licenseInfo.getCompanyId()), MAX_PAD, PAD_CHAR) +
                StringUtils.leftPad(decimalToBase(licenseInfo.getLicenseNo()), MAX_PAD, PAD_CHAR) +
                decimalToBase(licenseInfo.getEdition()) + decimalToBase(checksum);

        activationKey = salt + encrypt(activationKey, privateKey);
        if (formatter != null)
            return formatter.format(activationKey);
        return activationKey;
    }

    @Override
    public String encode() {
        return encode(new DefaultFormatter());
    }

    public String encode(int companyId, int licenseNo, int edition) {
        licenseInfo = new LicenseInfo(companyId, licenseNo, edition);
        return encode();
    }

    private class DefaultFormatter implements IKeyFormatter {
        @Override
        public String format(String key) {
            return String.format("%s-%s-%s", key.substring(0, 3), key.substring(3, 6), key.substring(6, 9)).trim().toUpperCase();
        }
    }

    @Override
    public ILicenseInfo decode(String inputKey) throws Exception {
        String salt = String.valueOf(inputKey.charAt(0));
        String privateKey = addSalt(PRIVATE_KEY, salt);
        String actualKey = inputKey.replace("-", "");
        if (actualKey.length() != 9) {
            throw new Exception("Invalid key. Length must be exactly 9 characters.");
        }

        String activationKey = actualKey.substring(1, actualKey.length());
        String encryptedKey = encrypt(activationKey, privateKey);

        int companyId = baseToDecimal(encryptedKey.substring(0, 3));
        int licenseNo = baseToDecimal(encryptedKey.substring(3, 6));
        int edition = baseToDecimal(encryptedKey.substring(6, 7));
        int checksum = baseToDecimal(encryptedKey.substring(7, 8));
        int calculatedChecksum = calculateChecksum(companyId, licenseNo, edition);

        if (checksum == calculatedChecksum) {
            return new LicenseInfo(companyId, licenseNo, edition);
        } else {
            throw new Exception("Invalid key.");
        }
    }

    public static int generateRandomInRange(int min, int max) {
        Random rand = new Random();
        return rand.nextInt(max - min + 1) + min;
    }

    public static String decimalToBase(int n) {
        return Integer.toString(n, BASE);
    }

    public static int baseToDecimal(String s) {
        return Integer.parseInt(s, BASE);
    }

    public static String encrypt(String s, String k) {
        int n = s.length();
        String output = "";
        for (int i = 0; i < n; i++) {
            int x = baseToDecimal(String.valueOf(k.charAt(i)));
            int y = baseToDecimal(String.valueOf(s.charAt(i)));
            String t = decimalToBase(x ^ y);
            output += t;
        }
        return output;
    }

    public static int calculateChecksum(int companyId, int licenseNo, int edition) {
        return (companyId + licenseNo + edition) % BASE;
    }

    public static String addSalt(String s, String k) {
        int n = s.length();
        char[] output = s.toCharArray();
        for (int i = 0; i < n; i++) {
            if (i % 2 == 0){
                int x = baseToDecimal(String.valueOf(s.charAt(i)));
                int y = baseToDecimal(k);
                String t = decimalToBase(x ^ y);
                output[i] = t.charAt(0);
            }
        }
        return String.valueOf(output);
    }
}
