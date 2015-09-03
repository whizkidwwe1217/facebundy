package com.jeonsoft.facebundypro.licensing;

/**
 * Created by WendellWayne on 2/14/2015.
 */
public enum AppEditions {
    Unknown,
    Basic,
    ExtractionOnly,
    ExtractionAndMatching;

    public static AppEditions fromInt(int x) {
        switch (x) {
            case 1:
                return Basic;
            case 2:
                return ExtractionOnly;
            case 3:
                return ExtractionAndMatching;
            default:
                return Unknown;
        }
    }

    public static int toInt(AppEditions edition) {
        if (edition == Basic)
            return 1;
        else if (edition == ExtractionOnly)
            return 2;
        else if (edition == ExtractionAndMatching)
            return 3;
        else
            return 0;
    }
}
