package com.winterpei.videolivewallpaper;

import android.util.Log;

/**
 * @author xingyang.pei
 * @date 2017/9/1.
 */

public class LogUtils {


    /**
     * 显示LOG(默认info级别)
     */
    public static void show(String TAG, String msg) {
        show(TAG, msg, Log.INFO);
    }

    /**
     * 显示LOG
     */
    public static void show(String TAG, String msg, int level) {
        switch (level) {
            case Log.VERBOSE:
                Log.v(TAG, msg);
                break;
            case Log.DEBUG:
                Log.d(TAG, msg);
                break;
            case Log.INFO:
                Log.i(TAG, msg);
                break;
            case Log.WARN:
                Log.w(TAG, msg);
                break;
            case Log.ERROR:
                Log.e(TAG, msg);
                break;
            default:
                Log.i(TAG, msg);
                break;
        }
    }
}
