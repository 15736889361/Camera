package com.chinamobile.gdwy;

import android.util.Log;

/**
 * Created by liangzhongtai on 2017/8/25.
 * 日志控制
 */

public class LogUtil {
    private static int LOG_MAXLENGTH = 300;
    private static boolean log = true;
    public static void setLog(boolean useLog){
        log = useLog;
    }
    public static void d(String tag, String msg) {
        if(!log)return;
        int strLength = msg.length();
        int start = 0;
        int end = LOG_MAXLENGTH;
        for (int i = 0; i < 1000; i++) {
            if (strLength > end) {
                Log.d(tag + i, msg.substring(start, end));
                start = end;
                end = end + LOG_MAXLENGTH;
            } else {
                Log.d(tag, msg.substring(start, strLength));
                break;
            }
        }
    }
}
