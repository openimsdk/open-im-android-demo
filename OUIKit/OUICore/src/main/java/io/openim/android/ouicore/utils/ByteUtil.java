package io.openim.android.ouicore.utils;

import java.text.DecimalFormat;

public class ByteUtil {
    private static final int GB = 1024 * 1024 *1024;
    private static final int MB = 1024 * 1024;
    private static final int KB = 1024;

    public static String bytes2kb(long bytes){
        DecimalFormat format = new DecimalFormat("###.0");
        if (bytes / GB >= 1){
            return format.format(bytes / GB) + "G";
        }
        else if (bytes / MB >= 1){
            return format.format(bytes / MB) + "M";
        }
        else if (bytes / KB >= 1){
            return format.format(bytes / KB) + "K";
        }else {
            return bytes + "B";
        }
    }

}
